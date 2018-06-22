package com.chuangkou.pdu.thread;

import com.chuangkou.pdu.controller.BaseController;
import com.chuangkou.pdu.controller.PduTemporaryController;
import com.chuangkou.pdu.entity.*;
import com.chuangkou.pdu.service.*;
import com.chuangkou.pdu.util.DeviceEvent;
import com.chuangkou.pdu.util.MessageStringDLTUtils;
import com.chuangkou.pdu.util.StringUtil;
import com.sun.xml.internal.rngom.parse.host.Base;
import net.sf.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @Author:
 * @Description:
 * @Date:Created in 10:07 2018/3/2
 */

//public class SubPolThread implements Callable<Boolean> {
public class SubPolThread implements Runnable {
    //    @Resource(name = "pduTemporaryService")
//    private static PduTemporaryService pduTemporaryService2;
//PduTemporaryService pduTemporaryService=(PduTemporaryService) SpringApplicationContextHolder.getSpringBean("pduTemporaryService");
//    @Resource(name = "pduService")
//    private static PduService pduService;
//
//    @Resource(name = "pduInfoService")
//    private static PduInfoService pduInfoService;
//
//    @Resource(name = "pduWarningSetService")
//    private static PduWarningSetService pduWarningSetService;
//
//    @Resource(name = "pduWarningService")
//    private static PduWarningService pduWarningService;

    private static Socket connection;
    public static String readMsg;
    boolean run = true;
    int num = 0;


    private static PduWarningSetService pduWarningSetService = (PduWarningSetService) SpringApplicationContextHolder.getSpringBean("pduWarningSetService");

    private static PduWarningService pduWarningService = (PduWarningService) SpringApplicationContextHolder.getSpringBean("pduWarningService");

    private static PduInfoService pduInfoService = (PduInfoService) SpringApplicationContextHolder.getSpringBean("pduInfoService");

    private static PduService pduService = (PduService) SpringApplicationContextHolder.getSpringBean("pduService");

    private static PduTemporaryService pduTemporaryService = (PduTemporaryService) SpringApplicationContextHolder.getSpringBean("pduTemporaryService");

    private static PduGroupRelationService pduGroupRelationService = (PduGroupRelationService) SpringApplicationContextHolder.getSpringBean("pduGroupRelationService");

    public SubPolThread(Socket conSocket) throws Exception {
        this.connection = conSocket;
//        this.call();
//        run();
    }


    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
//    public Boolean call() throws Exception {
    public void run() {


        try {
            while (true) {
                //                Thread.sleep(1000);
                String msg = "";
                msg = ThreadUtils.readMessageFromClient(connection.getInputStream());
                //                System.out.println("收到报文==" + msg);
                //                setReadMsg(msg);

                Message message = new Message();
                MessageDLT messageDLT = new MessageDLT();

                String dataTab = "06EE0100";//预警报文
                dataTab = MessageStringDLTUtils.dateIDhex(dataTab);

                if (!msg.equals("")) {

                    messageDLT = MessageStringDLTUtils.onlineMessage(msg);
                    String macheineID = messageDLT.getMachineAddress();
                    macheineID = MessageStringDLTUtils.machineAddressHexOpposite(macheineID);//正序

                    if (msg.substring(0, 8).equals("FEFEFEFE")) {
                        BaseController.readmsg = msg;
                        System.out.println("收到回复的报文==" + BaseController.readmsg);
                        //                        Thread.sleep(1000);
                        break;
                    }

                    //判断是心跳包
                    if (messageDLT.getDataTab().equals("3C3C3239") && messageDLT.getDataStr().equals("55555555")) {//设备上电发送报文，核对IP地址和设备ID
                        System.out.println("这是心跳包！");

                        messageDLT.setControl("2E");
                        messageDLT.setMachineAddress(macheineID);

                        String heartmsg = getHeartMessage(macheineID);

                        System.out.println("发送心跳==" + heartmsg);

                        ThreadUtils.writeMsgToClient(connection.getOutputStream(), heartmsg);

                        String threadIp = this.connection.getInetAddress().toString();
                        message.setIp(threadIp.substring(1, threadIp.length()));
                        String ip = message.getIp();

                        //                        String machineID = messageDLT.getMachineAddress();
                        //                        machineID = MessageStringDLTUtils.machineAddressHex(machineID);

                        //设备上电对校时
                        Thread.sleep(2000);
                        Thread threadTime = new Thread(new JobPduDateTimeSetThread(connection, messageDLT.getMachineAddress()));
                        threadTime.start();
                        threadTime.join();

                        Pdu searchPdu = new Pdu();
                        //                        searchPdu.setMachineid(machineID);
                        System.out.println("macheineID== " + macheineID);
                        searchPdu = pduService.selectByMachineID(macheineID);

                        if (searchPdu != null) {

                            //判断IP地址不同 状态为离线的 修改设备信息
                            if (!searchPdu.getIp().equals(ip) || searchPdu.getOnlinestate().equals("2")) {

                                System.out.println("设备存在，但是IP地址有变化");
                                String updateAction = ThreadUtils.updatePduIpDLT(messageDLT, ip, this.connection);
                                Pdu pduaddress = pduService.selectByMachineID(macheineID);


                                //设备设备上线 推送消息提醒
                                System.out.println("设备上线推送消息====");
                                int event_type = DeviceEvent.TYPE_ONLINE;//下线事件类型
                                String appMessage = ThreadUtils.sendDeviceEvent(pduaddress.getId(), event_type);
                                ThreadUtils.connectionMapMessageSend(BaseController.APPSubPolmap, appMessage);

                            } else {

                                //                                BaseController.SubPolmap.put(addressip, connection);
                            }
                        }

                        if (searchPdu == null) {

                            //新设备
                            String updateAction = ThreadUtils.updatePduIpDLT(messageDLT, ip, this.connection);
                            //                            BaseController.SubPolmap.put(addressip, connection);

                            Pdu pduaddress = pduService.selectByMachineID(macheineID);

                            //设备上电对校时
                            //                            System.out.println("设备上电对校时====");
                            threadTime = new Thread(new JobPduDateTimeSetThread(connection, pduaddress.getMachineid()));
                            threadTime.start();
                            threadTime.join();

                            //设备设备上线 推送消息提醒
                            System.out.println("设备上线推送消息====");
                            int event_type = DeviceEvent.TYPE_ONLINE;//下线事件类型
                            String appMessage = ThreadUtils.sendDeviceEvent(pduaddress.getId(), event_type);
                            ThreadUtils.connectionMapMessageSend(BaseController.APPSubPolmap, appMessage);

                        }
                        //                        }

                    }


                    //判断是预警信息
                    if (messageDLT.getDataTab().equals(dataTab)) {
                        PduWarning pduWarning = new PduWarning();
                        System.out.println("收到预警信息！编号：" + macheineID);
                        Pdu warningPdu = pduService.selectByMachineID(macheineID);

                        if (warningPdu != null) {
                            //                            String dataStr = messageDLT.getDataStr();
                            //                            String warningType = dataStr.substring(8, dataStr.length());
                            String warningType = messageDLT.getDataStr();
                            warningType = MessageStringDLTUtils.receiverVlue(warningType);

                            pduWarning.setWarningtype(warningType);
                            pduWarning.setPduid(warningPdu.getId());
                            pduWarning.setState("0");

                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String datetime = df.format(System.currentTimeMillis());
                            pduWarning.setWarningtime(datetime);
                            pduWarningService.insert(pduWarning);//插入上报的预警信息

                            Pdu updatePdu = new Pdu();
                            System.out.println("warningPdu.getId()===" + warningPdu.getId());
                            updatePdu.setId(warningPdu.getId());
                            updatePdu.setOnlinestate("3");
                            pduService.update(updatePdu);//修改设备状态为 发生故障

                            //设备故障 推送消息提醒 至APP
                            int event_type = 100;
                            switch (Integer.valueOf(warningType)) {
                                case 0:
                                    event_type = DeviceEvent.TYPE_ELECTRIC_LEAKAGE;//漏电事件类型
                                    break;
                                case 1:
                                    event_type = DeviceEvent.TYPE_OPEN_CIRCUIT;//断路事件类型
                                    break;
                                case 2:
                                    event_type = DeviceEvent.TYPE_POWER_ABNORMAL;//功率事件类型
                                    break;
                                case 3:
                                    event_type = DeviceEvent.TYPE_OVER_VOLTAGE;//过压事件类型
                                    break;
                                case 4:
                                    event_type = DeviceEvent.TYPE_UNDER_VOLTAGE;//欠压事件类型
                                    break;
                                case 5:
                                    event_type = DeviceEvent.TYPE_OVER_CURRENT;//过流事件类型
                                    break;
                                case 6:
                                    event_type = DeviceEvent.TYPE_SMOKE_OPEN;//烟雾事件类型
                                    break;
                                case 7:
                                    event_type = DeviceEvent.TYPE_TEMPERATURE_OPEN;//温度事件类型
                                    break;
                                case 8:
                                    event_type = DeviceEvent.TYPE_WATERLEVEL_OPEN;//液位事件类型
                                    break;
                            }

                            String appMessage = ThreadUtils.sendDeviceEvent(warningPdu.getId(), event_type);
                            ThreadUtils.connectionMapMessageSend(BaseController.APPSubPolmap, appMessage);
                        } else {
                            System.out.println("设备：" + macheineID + "不存在");
                        }
                        //                        BaseController.SubPolmap.put(addressip, connection);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


/**
 String addressip = connection.getInetAddress().toString();
 addressip = addressip.substring(1, addressip.length());
 int pduid = 0;
 int num = 0;
 try {
 //            while (run) {
 Thread.sleep(4000);
 //读取客户端传过来的数据报文
 String msg = "";
 Message message = new Message();
 MessageDLT messageDLT = new MessageDLT();

 //                while(msg.equals("")){
 msg = ThreadUtils.readMessageFromClient(connection.getInputStream());
 //                }


 System.out.println(connection.getInetAddress().toString() + "===" + msg);


 //                System.out.println("收到报文==" + msg);
 String dataTab = "06EE0100";//预警报文
 dataTab = MessageStringDLTUtils.dateIDhex(dataTab);

 if (!msg.equals("")) {

 messageDLT = MessageStringDLTUtils.onlineMessage(msg);
 String macheineID = messageDLT.getMachineAddress();
 macheineID = MessageStringDLTUtils.machineAddressHexOpposite(macheineID);//正序

 //判断是心跳包
 if (messageDLT.getDataTab().equals("3C3C3239") && messageDLT.getDataStr().equals("55555555")) {//设备上电发送报文，核对IP地址和设备ID
 System.out.println("这是心跳包！");

 messageDLT.setControl("2E");
 messageDLT.setMachineAddress(macheineID);

 String heartmsg = getHeartMessage(macheineID);

 //                        String heartmsg = msg.replaceAll("11", "2E");
 //                        String sum = MessageStringDLTUtils.makeChecksum(heartmsg.substring(0, heartmsg.length() - 4));
 //                        heartmsg = heartmsg.substring(0, heartmsg.length() - 4) + sum + "160D0A";

 System.out.println("发送心跳==" + heartmsg);

 ThreadUtils.writeMsgToClient(connection.getOutputStream(), heartmsg);

 String readheart = ThreadUtils.readMessageFromClient(connection.getInputStream());

 //                        while (readheart.equals("")) {
 //                            System.out.println("重发心跳===" + heartmsg);
 //                            ThreadUtils.writeMsgToClient(connection.getOutputStream(), heartmsg);
 //
 //                            readheart = ThreadUtils.readMessageFromClient(connection.getInputStream());
 //                            System.out.println("readheart===" + readheart);
 //                        }

 //                        if (readheart.equals("")) {
 //                            messageDLT = MessageStringDLTUtils.onlineMessage(readheart);
 //                            String newddress = MessageStringDLTUtils.machineAddressHexOpposite(messageDLT.getMachineAddress());
 //                            if (messageDLT.getControl().equals("91") && macheineID.equals(newddress)) {
 //                                System.out.println("收到心跳！");
 //                            }


 String threadIp = this.connection.getInetAddress().toString();
 message.setIp(threadIp.substring(1, threadIp.length()));
 String ip = message.getIp();

 String machineID = messageDLT.getMachineAddress();
 machineID = MessageStringDLTUtils.machineAddressHex(machineID);

 //设备上电对校时
 //                                System.out.println("设备上电对校时====");
 Thread threadTime = new Thread(new JobPduDateTimeSetThread(connection, machineID));
 threadTime.start();
 threadTime.join();


 Pdu searchPdu = new Pdu();
 //                        searchPdu.setMachineid(machineID);
 searchPdu = pduService.selectByMachineID(machineID);

 if (searchPdu != null) {

 //判断IP地址不同 状态为离线的 修改设备信息
 if (!searchPdu.getIp().equals(ip) || searchPdu.getOnlinestate().equals("2")) {

 System.out.println("设备存在，但是IP地址有变化");
 String updateAction = ThreadUtils.updatePduIpDLT(messageDLT, ip, this.connection);
 //                                BaseController.SubPolmap.put(addressip, connection);

 Pdu pduaddress = pduService.selectByMachineID(macheineID);



 //设备设备上线 推送消息提醒
 System.out.println("设备上线推送消息====");
 int event_type = DeviceEvent.TYPE_ONLINE;//下线事件类型
 String appMessage = ThreadUtils.sendDeviceEvent(pduaddress.getId(), event_type);
 ThreadUtils.connectionMapMessageSend(BaseController.APPSubPolmap, appMessage);

 } else {

 //                                BaseController.SubPolmap.put(addressip, connection);
 }
 }

 if (searchPdu == null) {

 //新设备
 String updateAction = ThreadUtils.updatePduIpDLT(messageDLT, ip, this.connection);
 //                            BaseController.SubPolmap.put(addressip, connection);

 Pdu pduaddress = pduService.selectByMachineID(macheineID);

 //设备上电对校时
 //                            System.out.println("设备上电对校时====");
 threadTime = new Thread(new JobPduDateTimeSetThread(connection, pduaddress.getMachineid()));
 threadTime.start();
 threadTime.join();

 //设备设备上线 推送消息提醒
 System.out.println("设备上线推送消息====");
 int event_type = DeviceEvent.TYPE_ONLINE;//下线事件类型
 String appMessage = ThreadUtils.sendDeviceEvent(pduaddress.getId(), event_type);
 ThreadUtils.connectionMapMessageSend(BaseController.APPSubPolmap, appMessage);

 }
 //                        }

 }


 //判断是预警信息
 if (messageDLT.getDataTab().equals(dataTab)) {
 PduWarning pduWarning = new PduWarning();

 Pdu warningPdu = pduService.selectByMachineID(messageDLT.getMachineAddress());

 //                            String dataStr = messageDLT.getDataStr();
 //                            String warningType = dataStr.substring(8, dataStr.length());
 String warningType = messageDLT.getDataStr();
 warningType = MessageStringDLTUtils.receiverVlue(warningType);

 pduWarning.setWarningtype(warningType);
 pduWarning.setPduid(warningPdu.getId());
 pduWarning.setState("0");

 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 String datetime = df.format(System.currentTimeMillis());
 pduWarning.setWarningtime(datetime);
 pduWarningService.insert(pduWarning);//插入上报的预警信息

 Pdu updatePdu = new Pdu();
 updatePdu.setId(warningPdu.getId());
 updatePdu.setOnlinestate("3");
 pduService.update(updatePdu);//修改设备状态为 发生故障

 //设备故障 推送消息提醒 至APP
 int event_type = 100;
 switch (Integer.valueOf(warningType)) {
 case 0:
 event_type = DeviceEvent.TYPE_ELECTRIC_LEAKAGE;//漏电事件类型
 break;
 case 1:
 event_type = DeviceEvent.TYPE_OPEN_CIRCUIT;//断路事件类型
 break;
 case 2:
 event_type = DeviceEvent.TYPE_POWER_ABNORMAL;//功率事件类型
 break;
 case 3:
 event_type = DeviceEvent.TYPE_OVER_VOLTAGE;//过压事件类型
 break;
 case 4:
 event_type = DeviceEvent.TYPE_UNDER_VOLTAGE;//欠压事件类型
 break;
 case 5:
 event_type = DeviceEvent.TYPE_OVER_CURRENT;//过流事件类型
 break;
 case 6:
 event_type = DeviceEvent.TYPE_SMOKE_OPEN;//烟雾事件类型
 break;
 case 7:
 event_type = DeviceEvent.TYPE_TEMPERATURE_OPEN;//温度事件类型
 break;
 case 8:
 event_type = DeviceEvent.TYPE_WATERLEVEL_OPEN;//液位事件类型
 break;
 }

 String appMessage = ThreadUtils.sendDeviceEvent(warningPdu.getId(), event_type);
 ThreadUtils.connectionMapMessageSend(BaseController.APPSubPolmap, appMessage);

 //                        BaseController.SubPolmap.put(addressip, connection);
 }

 //判断是设备手动拉闸、合闸
 if (messageDLT.getControl().equals("1C") && messageDLT.getDataTab().equals("35434343")) {

 String datastr = messageDLT.getDataStr();
 //判断拉闸
 if (datastr.indexOf("4D") != -1) {
 //收到手动拉闸的消息，修改设备的继电器状态
 System.out.println("收到手动拉闸的消息,改设备的继电器状态");
 Pdu actionPdu = new Pdu();
 actionPdu = pduService.selectByMachineID(macheineID);
 actionPdu.setActionState("0");
 actionPdu.setOnlinestate("0");

 pduService.update(actionPdu);
 }

 //判断合闸
 if (datastr.indexOf("4E") != -1) {
 System.out.println("收到手动合闸的消息，修改设备的继电器状态");
 Pdu actionPdu = new Pdu();
 actionPdu = pduService.selectByMachineID(macheineID);
 actionPdu.setActionState("1");
 actionPdu.setOnlinestate("1");
 pduService.update(actionPdu);
 }

 }

 }

 if (msg.equals("") && num == 0) {
 Pdu pduinfo = new Pdu();
 pduinfo.setIp(addressip);
 List<Pdu> list = pduService.selectAllBySearch(pduinfo);

 if (list != null && list.size() > 0) {
 Pdu pduaddressResult = new Pdu();
 pduaddressResult = list.get(0);

 if (pduaddressResult.getType().equals("180") && msg.equals("") && pduaddressResult.getMachineid().length() == 10) {//判断是空开设备

 //                            BaseController.SubPolmap.put(addressip, connection);
 //集中器应该给多个设备 进行校正
 for (int i = 0; i < list.size(); i++) {
 pduaddressResult = list.get(i);
 //                                System.out.println("空开校时====");
 Thread threadTime = new Thread(new JobPduDateTimeSetThread(connection, pduaddressResult.getMachineid()));
 threadTime.start();
 threadTime.join();
 }
 num++;
 //                        break;
 } else {
 //                            connection.close();
 //                            break;
 }

 } else {
 //                        connection.close();
 //                        break;
 }

 }
 //            }
 //                connection.close();
 } catch(Exception e){
 //                BaseController.SubPolmap.remove(addressip);
 //                System.out.println(connection.getInetAddress() + "连接异常，已断开连接！");
 //                连接异常 推送消息提醒
 //                int event_type = DeviceEvent.TYPE_OFFLINE;//下线事件类型
 //                String appMessage = ThreadUtils.sendDeviceEvent(pduid, event_type);
 //                ThreadUtils.connectionMapMessageSend(BaseController.APPSubPolmap, appMessage);

 e.printStackTrace();
 //                break;
 }
 //        return true;
 //        }

 **/
}


        /**
         * 读取客户端信息
         *
         * @param inputStream
         */

    private static String readMessageFromClient(InputStream inputStream) throws IOException {
//        Reader reader = new InputStreamReader(inputStream);
//        BufferedReader br = new BufferedReader(reader);
//        String a = null;
//        String message = "";
//
//        //循环接收报文
//        while ((a = br.readLine()) != null) {
//            message += a;
//            System.out.println(message);
//            return message;
//        }
//        System.out.println("已接收到客户端连接");
//        BufferedReader bufferedReader = new BufferedReader(reader);//加入缓冲区
//        String temp = null;
//        String info = "";

//        while ((temp = bufferedReader.readLine()) != null) {
//            info += temp;
//            System.out.println("已接收到客户端连接");
//            System.out.println("服务端接收到客户端信息：" + info );
//        }

        String message = "";
        try {
            byte[] bytes = null;
            int bufflenth = inputStream.available();
            if (bufflenth > 0) {
                while (bufflenth != 0) {
                    // 初始化byte数组为buffer中数据的长度
                    bytes = new byte[bufflenth];
                    inputStream.read(bytes);
                    bufflenth = inputStream.available();
                }
                message = StringUtil.str2HexStr(bytes);
                System.out.println("接收到新报文：" + message);
            }
        } catch (Exception e) {
            System.out.println("接收报文异常！");
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return message;

    }

    /**
     * 响应客户端信息
     *
     * @param outputStream
     * @param string
     */
    private static void writeMsgToClient(OutputStream outputStream, String string) throws IOException {
//        Writer writer = new OutputStreamWriter(outputStream);
//        writer.append(string);
//        writer.flush();
//        writer.close();
        try {
//            System.out.println("发送的报文为：" + StringUtil.hexToBytes(string));
//            outputStream.write(StringUtil.hexToBytes(string));//必须十六进制转byte类型才能进行控制
//            outputStream.flush();
            byte[] ss = StringUtil.hexStringToByteArray(string);
            System.out.println("回复的报文为===" + ss.toString());
            outputStream.write(ss);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("发送报文异常！");
            e.printStackTrace();
        }

    }

    public static String getHeartMessage(String machineAddress) {
        String mesg = "";
        machineAddress = MessageStringDLTUtils.addZeroForNumLeft(machineAddress, 12);
        machineAddress = MessageStringDLTUtils.machineAddressHex(machineAddress);
        mesg = "68" + machineAddress + "682E083C3C323955555555";

        String datesum = MessageStringDLTUtils.makeChecksum(mesg);
        mesg = mesg + datesum + "160D0A";

        return mesg;

    }

    public static String getReadMsg() {
        return readMsg;
    }

    public static void setReadMsg(String readMsg) {
        SubPolThread.readMsg = readMsg;
    }
}
