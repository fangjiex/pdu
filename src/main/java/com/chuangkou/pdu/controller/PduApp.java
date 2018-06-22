package com.chuangkou.pdu.controller;

import com.chuangkou.pdu.bean.*;
import com.chuangkou.pdu.entity.*;
import com.chuangkou.pdu.service.*;
import com.chuangkou.pdu.thread.PduActionThread;
import com.chuangkou.pdu.thread.PduClockSetThread;
import com.chuangkou.pdu.thread.PduWarningSetThread;
import com.chuangkou.pdu.util.LogUtil;
import com.chuangkou.pdu.util.StringUtil;
import com.chuangkou.pdu.util.TokenUtil;
import com.chuangkou.pdu.util.PropertiesUtils;
import com.google.gson.Gson;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author:
 * @Description:
 * @Date:Created in 16:53 2018/4/3
 */
@Controller
public class PduApp extends BaseController {
    @Resource(name = "pduService")
    private PduService pduService;

    @Resource(name = "pduInfoService")
    private PduInfoService pduInfoService;

    @Resource(name = "roleService")
    private RoleService roleService;

    @Resource(name = "pduClockService")
    private PduClockService pduClockService;

    @Resource(name = "pduTemporaryService")
    private PduTemporaryService pduTemporaryService;

    @Resource(name = "pduGroupRelationService")
    private PduGroupRelationService pduGroupRelationService;


    @Resource(name = "pduRelationService")
    private PduRelationService pduRelationService;


    @Resource(name = "pduOldLineService")
    private PduOldLineService pduOldLineService;

    @Autowired
    private PduGroupService pduGroupService;

    private Pdu pdu = new Pdu();

    public Pdu getPdus() {
        return pdu;
    }

    public void setPdus(Pdu pdu) {
        this.pdu = pdu;
    }


    /**
     * @Author:xulei
     * @Description:获取设备功率记录
     * @Date:2018-03-21
     */
    @RequestMapping("/device/get_power_history")
    @ResponseBody
    public void jsonPowerHistory(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String access_token = request.getHeader("access_token");
        String device_id = request.getParameter("device_id");
        String start_date = request.getParameter("start_date");
        String end_date = request.getParameter("end_date");
        String query_type = request.getParameter("query_type");

        start_date = start_date + " 00:00:00";
        end_date = end_date + " 23:59:59";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        start_date = sdf.parse(start_date).toString();
//        end_date = sdf.parse(end_date).toString();
        int ID = Integer.valueOf(device_id);

        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {

            try {
                if (query_type.equals("0")) {

                    PduInfo pduInfo = new PduInfo();
                    pduInfo.setPduid(ID);
                    pduInfo.setStarttime(start_date);
                    pduInfo.setEndtime(end_date);
                    List<PduInfo> infoList = new ArrayList<PduInfo>();

                    infoList = pduInfoService.selectAllByHistoryData(pduInfo);

                    GetPduInfoHistory getPduInfoHistory = new GetPduInfoHistory();
                    List<GetPduInfoHistory.HistoryListBean> powerList = new ArrayList<GetPduInfoHistory.HistoryListBean>();

                    if (infoList.size() > 0) {

                        for (int i = 0; i < infoList.size(); i++) {
                            GetPduInfoHistory.HistoryListBean historyListBean = new GetPduInfoHistory.HistoryListBean();
                            PduInfo pduInfoTwo = new PduInfo();
                            pduInfoTwo = infoList.get(i);

                            historyListBean.setDatetime(pduInfoTwo.getCollectiontime() + " 00:00:00");
                            historyListBean.setValue(Float.parseFloat(pduInfoTwo.getWatt()));

                            powerList.add(historyListBean);
                        }
                        getPduInfoHistory.setQuery_type(Integer.valueOf(query_type));
                        getPduInfoHistory.setHistory_list(powerList);

                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getInstance();
                        msgBean.setData(getPduInfoHistory);

                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();

                    } else {

                        String errormsg = "该时间区域内，设备" + device_id + "没有功率记录";
                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getFalseInstance(errormsg);
                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();
                    }
                } else {

                    PduInfo pduInfo = new PduInfo();
                    pduInfo.setPduid(ID);
                    pduInfo.setStarttime(start_date);
                    pduInfo.setEndtime(end_date);
                    List<PduInfo> infoList = new ArrayList<PduInfo>();

                    infoList = pduInfoService.selectAllByHistoryHours(pduInfo);

                    GetPduInfoHistory getPduInfoHistory = new GetPduInfoHistory();
                    List<GetPduInfoHistory.HistoryListBean> powerList = new ArrayList<GetPduInfoHistory.HistoryListBean>();

                    if (infoList.size() > 0) {

                        for (int i = 0; i < infoList.size(); i++) {
                            GetPduInfoHistory.HistoryListBean historyListBean = new GetPduInfoHistory.HistoryListBean();
                            PduInfo pduInfoTwo = new PduInfo();
                            pduInfoTwo = infoList.get(i);

                            historyListBean.setDatetime(pduInfoTwo.getCollectiontime());
                            historyListBean.setValue(Float.parseFloat(pduInfoTwo.getWatt()));

                            powerList.add(historyListBean);
                        }
                        getPduInfoHistory.setQuery_type(Integer.valueOf(query_type));
                        getPduInfoHistory.setHistory_list(powerList);

                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getInstance();
                        msgBean.setData(getPduInfoHistory);

                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();

                    } else {

                        String errormsg = "该时间区域内，设备" + device_id + "没有功率记录";
                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getFalseInstance(errormsg);
                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();
                    }


                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            PrintWriter out = response.getWriter();
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }

    }

    /**
     * @Author:xulei
     * @Description:获取设备电流记录
     * @Date:2018-03-21
     */
    @RequestMapping("/device/get_electricity_history")
    @ResponseBody
    public void jsonElectricityHistory(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String access_token = request.getHeader("access_token");
        String device_id = request.getParameter("device_id");
        String start_date = request.getParameter("start_date");
        String end_date = request.getParameter("end_date");
        String query_type = request.getParameter("query_type");


        start_date = start_date + " 00:00:00";
        end_date = end_date + " 23:59:59";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        start_date = sdf.parse(start_date).toString();
//        end_date = sdf.parse(end_date).toString();
        int ID = Integer.valueOf(device_id);
        //验证并解析token
//        access_token = "admin,21232F297A57A5A743894A0E4A801FC3,1528857680478";
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            try {
                if (query_type.equals("0")) {

                    PduInfo pduInfo = new PduInfo();
                    pduInfo.setPduid(ID);
                    pduInfo.setStarttime(start_date);
                    pduInfo.setEndtime(end_date);
                    List<PduInfo> infoList = new ArrayList<PduInfo>();

                    infoList = pduInfoService.selectAllByHistoryData(pduInfo);

                    GetPduInfoHistory getPduInfoHistory = new GetPduInfoHistory();
                    List<GetPduInfoHistory.HistoryListBean> currentList = new ArrayList<GetPduInfoHistory.HistoryListBean>();

                    if (infoList.size() > 0) {

                        for (int i = 0; i < infoList.size(); i++) {
                            GetPduInfoHistory.HistoryListBean historyListBean = new GetPduInfoHistory.HistoryListBean();
                            PduInfo pduInfoTwo = new PduInfo();
                            pduInfoTwo = infoList.get(i);

                            historyListBean.setDatetime(pduInfoTwo.getCollectiontime() + " 00:00:00");
                            historyListBean.setValue(Float.parseFloat(pduInfoTwo.getCurrent()));

                            currentList.add(historyListBean);
                        }
                        getPduInfoHistory.setQuery_type(Integer.valueOf(query_type));
                        getPduInfoHistory.setHistory_list(currentList);

                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getInstance();
                        msgBean.setData(getPduInfoHistory);

                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();

                    } else {

                        String errormsg = "该时间区域内，设备" + device_id + "没有电流记录";
                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getFalseInstance(errormsg);
                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();
                    }
                } else {

                    PduInfo pduInfo = new PduInfo();
                    pduInfo.setPduid(ID);
                    pduInfo.setStarttime(start_date);
                    pduInfo.setEndtime(end_date);
                    List<PduInfo> infoList = new ArrayList<PduInfo>();

                    infoList = pduInfoService.selectAllByHistoryHours(pduInfo);

                    GetPduInfoHistory getPduInfoHistory = new GetPduInfoHistory();
                    List<GetPduInfoHistory.HistoryListBean> currentList = new ArrayList<GetPduInfoHistory.HistoryListBean>();

                    if (infoList.size() > 0) {

                        for (int i = 0; i < infoList.size(); i++) {
                            GetPduInfoHistory.HistoryListBean historyListBean = new GetPduInfoHistory.HistoryListBean();
                            PduInfo pduInfoTwo = new PduInfo();
                            pduInfoTwo = infoList.get(i);

                            historyListBean.setDatetime(pduInfoTwo.getCollectiontime());
                            historyListBean.setValue(Float.parseFloat(pduInfoTwo.getCurrent()));

                            currentList.add(historyListBean);
                        }
                        getPduInfoHistory.setQuery_type(Integer.valueOf(query_type));
                        getPduInfoHistory.setHistory_list(currentList);

                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getInstance();
                        msgBean.setData(getPduInfoHistory);

                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();

                    } else {

                        String errormsg = "该时间区域内，设备" + device_id + "没有电流记录";
                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getFalseInstance(errormsg);
                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();
                    }


                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            PrintWriter out = response.getWriter();
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }

    }


    /**
     * @Author:xulei
     * @Description:获取设备电压记录
     * @Date:2018-03-21
     */
    @RequestMapping("/device/get_voltage_history")
    @ResponseBody
    public void jsonVoltageHistory(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String device_id = request.getParameter("device_id");
        String start_date = request.getParameter("start_date");
        String end_date = request.getParameter("end_date");
        String query_type = request.getParameter("query_type");


        start_date = start_date + " 00:00:00";
        end_date = end_date + " 23:59:59";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        start_date = sdf.parse(start_date).toString();
//        end_date = sdf.parse(end_date).toString();
        int ID = Integer.valueOf(device_id);
        PrintWriter out = response.getWriter();
        String access_token = request.getHeader("access_token");
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            try {
                if (query_type.equals("0")) {

                    PduInfo pduInfo = new PduInfo();
                    pduInfo.setPduid(ID);
                    pduInfo.setStarttime(start_date);
                    pduInfo.setEndtime(end_date);
                    List<PduInfo> infoList = new ArrayList<PduInfo>();

                    infoList = pduInfoService.selectAllByHistoryData(pduInfo);

                    GetPduInfoHistory getPduInfoHistory = new GetPduInfoHistory();
                    List<GetPduInfoHistory.HistoryListBean> voltageList = new ArrayList<GetPduInfoHistory.HistoryListBean>();

                    if (infoList.size() > 0) {

                        for (int i = 0; i < infoList.size(); i++) {
                            GetPduInfoHistory.HistoryListBean historyListBean = new GetPduInfoHistory.HistoryListBean();
                            PduInfo pduInfoTwo = new PduInfo();
                            pduInfoTwo = infoList.get(i);

                            historyListBean.setDatetime(pduInfoTwo.getCollectiontime() + " 00:00:00");
                            historyListBean.setValue(Float.parseFloat(pduInfoTwo.getVoltage()));

                            voltageList.add(historyListBean);
                        }


                        getPduInfoHistory.setQuery_type(Integer.valueOf(query_type));
                        getPduInfoHistory.setHistory_list(voltageList);

                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getInstance();
                        msgBean.setData(getPduInfoHistory);

                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();

                    } else {

                        String errormsg = "该时间区域内，设备" + device_id + "没有电压记录";
                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getFalseInstance(errormsg);
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();
                    }

                } else {//今天的数据 按小时返回
                    PduInfo pduInfo = new PduInfo();
                    pduInfo.setPduid(ID);
                    pduInfo.setStarttime(start_date);
                    pduInfo.setEndtime(end_date);
                    List<PduInfo> infoList = new ArrayList<PduInfo>();

                    infoList = pduInfoService.selectAllByHistoryHours(pduInfo);

                    GetPduInfoHistory getPduInfoHistory = new GetPduInfoHistory();
                    List<GetPduInfoHistory.HistoryListBean> voltageList = new ArrayList<GetPduInfoHistory.HistoryListBean>();
                    if (infoList.size() > 0) {

                        for (int i = 0; i < infoList.size(); i++) {
                            GetPduInfoHistory.HistoryListBean historyListBean = new GetPduInfoHistory.HistoryListBean();
                            PduInfo pduInfoTwo = new PduInfo();
                            pduInfoTwo = infoList.get(i);

                            historyListBean.setDatetime(pduInfoTwo.getCollectiontime());
                            historyListBean.setValue(Float.parseFloat(pduInfoTwo.getVoltage().toString()));

                            voltageList.add(historyListBean);
                        }

                        getPduInfoHistory.setQuery_type(Integer.valueOf(query_type));
                        getPduInfoHistory.setHistory_list(voltageList);

                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getInstance();
                        msgBean.setData(getPduInfoHistory);

                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();

                    } else {

                        String errormsg = "该时间区域内，设备" + device_id + "没有电压记录";
                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getFalseInstance(errormsg);
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }

    }


    /**
     * @Author:xulei
     * @Description:获取设备电量记录
     * @Date:2018-0608
     */
    @RequestMapping("/device/get_quantity_history")
    @ResponseBody
    public void jsonQuantityHistory(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String device_id = request.getParameter("device_id");
        String start_date = request.getParameter("start_date");
        String end_date = request.getParameter("end_date");
        String query_type = request.getParameter("query_type");


        start_date = start_date + " 00:00:00";
        end_date = end_date + " 23:59:59";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        start_date = sdf.parse(start_date).toString();
//        end_date = sdf.parse(end_date).toString();
        int ID = Integer.valueOf(device_id);
        PrintWriter out = response.getWriter();
        String access_token = request.getHeader("access_token");

//        access_token = "admin,21232F297A57A5A743894A0E4A801FC3,1528857680478";
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            try {
                if (query_type.equals("0")) {

                    PduInfo pduInfo = new PduInfo();
                    pduInfo.setPduid(ID);
                    pduInfo.setStarttime(start_date);
                    pduInfo.setEndtime(end_date);
                    List<PduInfo> infoList = new ArrayList<PduInfo>();

                    infoList = pduInfoService.selectAllByHistoryData(pduInfo);

                    GetPduInfoHistory getPduInfoHistory = new GetPduInfoHistory();
                    List<GetPduInfoHistory.HistoryListBean> quantityList = new ArrayList<GetPduInfoHistory.HistoryListBean>();

                    if (infoList.size() > 0) {

                        for (int i = 0; i < infoList.size(); i++) {
                            GetPduInfoHistory.HistoryListBean historyListBean = new GetPduInfoHistory.HistoryListBean();
                            PduInfo pduInfoTwo = new PduInfo();
                            pduInfoTwo = infoList.get(i);

                            historyListBean.setDatetime(pduInfoTwo.getCollectiontime() + " 00:00:00");
                            historyListBean.setValue(Float.parseFloat(pduInfoTwo.getQuantity() == null ? "0" : pduInfoTwo.getQuantity()));

                            quantityList.add(historyListBean);
                        }


                        getPduInfoHistory.setQuery_type(Integer.valueOf(query_type));
                        getPduInfoHistory.setHistory_list(quantityList);

                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getInstance();
                        msgBean.setData(getPduInfoHistory);

                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();

                    } else {

                        String errormsg = "该时间区域内，设备" + device_id + "没有电量记录";
                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getFalseInstance(errormsg);
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();
                    }

                } else {//今天的数据 按小时返回
                    PduInfo pduInfo = new PduInfo();
                    pduInfo.setPduid(ID);
                    pduInfo.setStarttime(start_date);
                    pduInfo.setEndtime(end_date);
                    List<PduInfo> infoList = new ArrayList<PduInfo>();

                    infoList = pduInfoService.selectAllByHistoryHours(pduInfo);

                    GetPduInfoHistory getPduInfoHistory = new GetPduInfoHistory();
                    List<GetPduInfoHistory.HistoryListBean> quantityList = new ArrayList<GetPduInfoHistory.HistoryListBean>();
                    if (infoList.size() > 0) {

                        for (int i = 0; i < infoList.size(); i++) {
                            GetPduInfoHistory.HistoryListBean historyListBean = new GetPduInfoHistory.HistoryListBean();
                            PduInfo pduInfoTwo = new PduInfo();
                            pduInfoTwo = infoList.get(i);

                            historyListBean.setDatetime(pduInfoTwo.getCollectiontime());
                            historyListBean.setValue(Float.parseFloat(pduInfoTwo.getQuantity().toString()));

                            quantityList.add(historyListBean);
                        }

                        getPduInfoHistory.setQuery_type(Integer.valueOf(query_type));
                        getPduInfoHistory.setHistory_list(quantityList);

                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getInstance();
                        msgBean.setData(getPduInfoHistory);

                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();

                    } else {

                        String errormsg = "该时间区域内，设备" + device_id + "没有电压记录";
                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getFalseInstance(errormsg);
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }

    }


    /**
     * @Author:xulei
     * @Description:APP控制继电器开关
     * @Date: 208-03-21
     */
    @RequestMapping("/device/switch_device_relay")
//    @ResponseBody
    public void jsonRelayCommand(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String device_id = request.getParameter("device_id");
        String relay = request.getParameter("relay");

        int ID = Integer.valueOf(device_id);
        PrintWriter out = response.getWriter();
        String access_token = request.getHeader("access_token");
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            try {
                //获取设备详情，IP地址
                Pdu relayPdu = new Pdu();
                relayPdu = pduService.selectByPrimaryKey(ID);
                String ip = relayPdu.getIp();
                Socket relayCon = null;
                //获取连接对象
                relayCon = (Socket) SubPolmap.get(ip);

                if (relayCon != null) {
                    //创建一个线程，发送报文
                    Thread threadRelay = new Thread(new PduActionThread(relayCon, relayPdu, relay));
//                    PduActionThread.pduRelayOffOn(relayCon, relayPdu, relay);
                    threadRelay.start();
                    threadRelay.join();

                    relayPdu.setActionState(relay);
                    relayPdu.setOnlinestate(relay);
                    pduService.update(relayPdu);//修改主表设备的继电器状态；

                    //添加日志
                    String action = "1".equals(relay) ? "打开设备：" : "关闭设备：";
                    LogUtil.addLog(token.getUsername(), action + relayPdu.getName());


                    MsgBean msgBean = MsgBean.getInstance();
                    out.print(msgBean.toJsonString());
                    out.flush();
                    out.close();
                } else {
                    String errormsg = "设备操作失败，请重试！";
                    MsgBean msgBean = MsgBean.getFalseInstance(errormsg);
                    out.print(msgBean.toJsonString());
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }

    }


    /**
     * @Author:xulei
     * @Description: APP接口，返回可添加的设备
     * @Date:2018-2-25
     */
    @RequestMapping("/device/get_new_device_list")
    @ResponseBody
    public void jsonPduTemp(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = null;
        PrintWriter out = response.getWriter();
        String access_token = request.getHeader("access_token");
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            try {
                request.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=UTF-8");

                List<PduTemporary> pduSearchList = pduTemporaryService.selectAll();//获取搜索的设备列表

                GetNewDeviceList getNewDeviceList = new GetNewDeviceList();
                List<GetNewDeviceList.NewDeviceListBean> newDeviceList = new ArrayList<GetNewDeviceList.NewDeviceListBean>();

                for (int y = 0; y < pduSearchList.size(); y++) {
                    GetNewDeviceList.NewDeviceListBean newDeviceBean = new GetNewDeviceList.NewDeviceListBean();
                    PduTemporary pduTemporary = new PduTemporary();
                    pduTemporary = pduSearchList.get(y);

                    newDeviceBean.setDevice_id(pduTemporary.getId());
                    newDeviceBean.setIp(pduTemporary.getIp() == null ? "" : pduTemporary.getIp());
                    newDeviceBean.setMachine_id(pduTemporary.getMachineid() == null ? "" : pduTemporary.getMachineid());
                    newDeviceBean.setQrcode(pduTemporary.getQrcode() == null ? "" : pduTemporary.getQrcode());
                    newDeviceBean.setState(Integer.valueOf(pduTemporary.getState() == null ? "2" : pduTemporary.getState()));
                    newDeviceBean.setType(pduTemporary.getType() == null ? "" : pduTemporary.getType());

                    newDeviceList.add(newDeviceBean);

                }

                getNewDeviceList.setDevice_list(newDeviceList);

                MsgBean<GetNewDeviceList> msgBean = MsgBean.getInstance();
                msgBean.setData(getNewDeviceList);
                out.print(msgBean.toJsonString());
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }

    }


    /**
     * @Author:xulei
     * @Description: APP接口，返回已添加的设备以及详细信息，返回 pduinfoTemp
     * @Date:2018-2-25
     */
    @RequestMapping("/device/get_group_device_info")
    @ResponseBody
    public void jsonPduGroupList(Model model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONObject json = null;
        PrintWriter out = response.getWriter();
        String access_token = request.getHeader("access_token");
        //access_token = "test001,E10ADC3949BA59ABBE56E057F20F883E,1527748347597";
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);

        if (token.getJieguo()) {
            try {
                request.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=UTF-8");
                String groupid = request.getParameter("group_id").toString() == null ? "0" : request.getParameter("group_id").toString();
//              String groupid = "0";//测试数据，临时使用
                List<PduInfoTemp> PduInfoTempList = new ArrayList<PduInfoTemp>();
                if (groupid.equals("0")) {//groupID为0则不区分分组  为1则区分分组
                    PduInfoTempList = pduService.pduInfoAllList(groupid, access_token);
                } else {
                    PduInfoTempList = pduService.pduInfoAllList(groupid, access_token);
                }
                GetGroupDeviceInfo getGroupDeviceInfo = new GetGroupDeviceInfo();
                List<GetGroupDeviceInfo.GroupDeviceInfoBean> devicelist = new ArrayList<GetGroupDeviceInfo.GroupDeviceInfoBean>();
                for (int j = 0; j < PduInfoTempList.size(); j++) {
                    GetGroupDeviceInfo.GroupDeviceInfoBean DeviceInfo = new GetGroupDeviceInfo.GroupDeviceInfoBean();
                    PduInfoTemp pduInfoTemp = new PduInfoTemp();
                    pduInfoTemp = PduInfoTempList.get(j);
                    DeviceInfo.setDevice_id(pduInfoTemp.getPduid().toString());
                    DeviceInfo.setDevice_name(pduInfoTemp.getName() == null ? "" : pduInfoTemp.getName());
                    DeviceInfo.setDevice_state(Integer.valueOf(pduInfoTemp.getOnlinestate() == null ? "1" : pduInfoTemp.getOnlinestate()));
                    DeviceInfo.setEnable(pduInfoTemp.getIfcontrol() == null ? "0" : pduInfoTemp.getIfcontrol());

                    DeviceInfo.setGroup_id(String.valueOf(pduInfoTemp.getPdugroupid() == null ? "1" : pduInfoTemp.getPdugroupid()));
                    DeviceInfo.setGroup_name(pduInfoTemp.getGroupname() == null ? "" : pduInfoTemp.getGroupname());
                    DeviceInfo.setPower(Float.parseFloat(pduInfoTemp.getWatt() == null ? "0.0" : pduInfoTemp.getWatt()));
                    DeviceInfo.setElectricity(Float.parseFloat(pduInfoTemp.getCurrent() == null ? "0.0" : pduInfoTemp.getCurrent()));
                    DeviceInfo.setVoltage(Float.parseFloat(pduInfoTemp.getVoltage() == null ? "0.0" : pduInfoTemp.getVoltage()));
                    DeviceInfo.setOvervoltage(Integer.valueOf(pduInfoTemp.getOvervoltage() == null ? "0" : pduInfoTemp.getOvervoltage()));
                    DeviceInfo.setUndervoltage(Integer.valueOf(pduInfoTemp.getUndervoltage() == null ? "0" : pduInfoTemp.getUndervoltage()));
                    DeviceInfo.setOvercurrent(Integer.valueOf(pduInfoTemp.getOvercurrent() == null ? "0" : pduInfoTemp.getOvercurrent()));
                    DeviceInfo.setOpen_circuit(Integer.valueOf(pduInfoTemp.getCircuitbreaker() == null ? "0" : pduInfoTemp.getCircuitbreaker()));
                    DeviceInfo.setElectric_leakage(Integer.valueOf(pduInfoTemp.getElectricleakage() == null ? "0" : pduInfoTemp.getElectricleakage()));
                    DeviceInfo.setRelay(Integer.valueOf(pduInfoTemp.getRelaystate() == null ? "1" : pduInfoTemp.getRelaystate()));
                    DeviceInfo.setIp(pduInfoTemp.getIp() == null ? "" : pduInfoTemp.getIp());
                    DeviceInfo.setMachine_id(pduInfoTemp.getMachineid() == null ? "" : pduInfoTemp.getMachineid());
                    DeviceInfo.setUptime(StringUtil.convertTimeToLong(pduInfoTemp.getUpdateTime() == null ? "" : pduInfoTemp.getUpdateTime().toString()));//可能需要格式化或修改类型
                    DeviceInfo.setPdutype(pduInfoTemp.getType() == null ? "" : pduInfoTemp.getType());

                    String fileName = "/config.properties";

                    String elect1 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages1()));
                    String elect2 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages2()));
                    String elect3 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages3()));
                    String elect4 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages4()));
                    String elect5 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages5()));
                    String elect6 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages6()));

                    DeviceInfo.setElectronictages1(elect1);
                    DeviceInfo.setElectronictages2(elect2);
                    DeviceInfo.setElectronictages3(elect3);
                    DeviceInfo.setElectronictages4(elect4);
                    DeviceInfo.setElectronictages5(elect5);
                    DeviceInfo.setElectronictages6(elect6);

                    devicelist.add(DeviceInfo);
                }
                getGroupDeviceInfo.setDevice_list(devicelist);
                MsgBean<GetGroupDeviceInfo> msgBean = MsgBean.getInstance();
                msgBean.setData(getGroupDeviceInfo);
                out.print(msgBean.toJsonString());
                out.flush();
                out.close();
            } catch (Exception e) {
                System.out.print("执行异常");
                e.printStackTrace();
            }

        } else {
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }

    }


    /**
     * @Author:xulei
     * @Description: 获取当个设备实时运行状态
     * @Date:208-03-09
     */
    @RequestMapping("/device/get_realtime_data")
    @ResponseBody
    public void jsonPduList(HttpServletRequest request, HttpServletResponse response) throws Exception {

        JSONObject json = null;
        String access_token = request.getHeader("access_token");
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String device_id = request.getParameter("device_id");//设备ID
        PrintWriter out = response.getWriter();

//        access_token = "admin,21232F297A57A5A743894A0E4A801FC3,1528703330244";
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            try {
//            String pduid = "1";//测试数据，设备ID 临时使用

                GetGroupDeviceInfo getGroupDeviceInfo = new GetGroupDeviceInfo();
//            List<GetGroupDeviceInfo.GroupDeviceInfoBean> devicelist = new ArrayList<GetGroupDeviceInfo.GroupDeviceInfoBean>();
                int ID = Integer.valueOf(device_id);
                PduInfoTemp pduInfoTemp = new PduInfoTemp();
                pduInfoTemp = pduService.pduInfoByPrimaryKey(ID);

                if (pduInfoTemp != null) {
                    GetGroupDeviceInfo.GroupDeviceInfoBean DeviceInfo = new GetGroupDeviceInfo.GroupDeviceInfoBean();
                    DeviceInfo.setDevice_id(pduInfoTemp.getPduid().toString());
                    DeviceInfo.setDevice_name(pduInfoTemp.getName() == null ? "" : pduInfoTemp.getName());
                    DeviceInfo.setDevice_state(Integer.valueOf(pduInfoTemp.getOnlinestate() == null ? "1" : pduInfoTemp.getOnlinestate()));

                    DeviceInfo.setGroup_id(String.valueOf(pduInfoTemp.getPdugroupid() == null ? "1" : pduInfoTemp.getPdugroupid()));
                    DeviceInfo.setGroup_name(pduInfoTemp.getGroupname() == null ? "" : pduInfoTemp.getGroupname());
                    DeviceInfo.setPower(Float.parseFloat(pduInfoTemp.getWatt() == null ? "0.0" : pduInfoTemp.getWatt()));
                    DeviceInfo.setElectricity(Float.parseFloat(pduInfoTemp.getCurrent() == null ? "0.0" : pduInfoTemp.getCurrent()));
                    DeviceInfo.setVoltage(Float.parseFloat(pduInfoTemp.getVoltage() == null ? "0.0" : pduInfoTemp.getVoltage()));
                    DeviceInfo.setOvervoltage(Integer.valueOf(pduInfoTemp.getOvervoltage() == null ? "0" : pduInfoTemp.getOvervoltage()));
                    DeviceInfo.setUndervoltage(Integer.valueOf(pduInfoTemp.getUndervoltage() == null ? "0" : pduInfoTemp.getUndervoltage()));
                    DeviceInfo.setOvercurrent(Integer.valueOf(pduInfoTemp.getOvercurrent() == null ? "0" : pduInfoTemp.getOvercurrent()));
                    DeviceInfo.setOpen_circuit(Integer.valueOf(pduInfoTemp.getCircuitbreaker() == null ? "0" : pduInfoTemp.getCircuitbreaker()));
                    DeviceInfo.setElectric_leakage(Integer.valueOf(pduInfoTemp.getElectricleakage() == null ? "0" : pduInfoTemp.getElectricleakage()));
                    DeviceInfo.setRelay(Integer.valueOf(pduInfoTemp.getRelaystate() == null ? "0" : pduInfoTemp.getRelaystate()));
                    DeviceInfo.setIp(pduInfoTemp.getIp() == null ? "" : pduInfoTemp.getIp());
                    DeviceInfo.setMachine_id(pduInfoTemp.getMachineid() == null ? "" : pduInfoTemp.getMachineid());
                    DeviceInfo.setUptime(StringUtil.convertTimeToLong(pduInfoTemp.getUpdateTime() == null ? "0" : pduInfoTemp.getUpdateTime()));//可能需要格式化或修改类型


                    if (pduInfoTemp.getType().equals("0000")) {
                        DeviceInfo.setPdutype("0");
                    }
                    if (pduInfoTemp.getType().equals("180")) {
                        DeviceInfo.setPdutype("1");
                    }
                    if (pduInfoTemp.getType().equals("0001")) {
                        DeviceInfo.setPdutype("2");
                    }
//                    DeviceInfo.setPdutype(pduInfoTemp.getType() == null ? "" : pduInfoTemp.getType());

                    String quantity = pduInfoTemp.getQuantity() == null ? "0.0" : pduInfoTemp.getQuantity();
                    DeviceInfo.setTotal_electricity(Double.valueOf(quantity));//当前电量

                    //计算月总电量
                    PduInfoTemp monthQuantity = new PduInfoTemp();
                    monthQuantity = pduService.pduInfoGetMonthQuantity(ID);

                    if (monthQuantity != null) {
                        String mquantity = monthQuantity.getMonthQuantity() == null ? "0.0" : monthQuantity.getMonthQuantity();
                        DeviceInfo.setMonth_electricity(Double.valueOf(mquantity));
                    } else {
                        DeviceInfo.setMonth_electricity(Double.valueOf("0.0"));
                    }
//                    String fileName = "/config.properties";
//
//                    String elect1 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages1()));
//                    String elect2 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages2()));
//                    String elect3 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages3()));
//                    String elect4 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages4()));
//                    String elect5 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages5()));
//                    String elect6 = PropertiesUtils.loaderGetValues(fileName, "pdu.electronictages." + String.valueOf(pduInfoTemp.getElectronictages6()));

                    DeviceInfo.setElectronictages1(pduInfoTemp.getElectronictages1());
                    DeviceInfo.setElectronictages2(pduInfoTemp.getElectronictages2());
                    DeviceInfo.setElectronictages3(pduInfoTemp.getElectronictages3());
                    DeviceInfo.setElectronictages4(pduInfoTemp.getElectronictages4());
                    DeviceInfo.setElectronictages5(pduInfoTemp.getElectronictages5());
                    DeviceInfo.setElectronictages6(pduInfoTemp.getElectronictages6());

                    getGroupDeviceInfo.setDevice_detail(DeviceInfo);

                    MsgBean<GetGroupDeviceInfo> msgBean = MsgBean.getInstance();
                    msgBean.setData(getGroupDeviceInfo);
                    out.print(msgBean.toJsonString());
                    out.flush();
                    out.close();

                } else {
                    String errormsg = "设备ID：" + device_id + " 不存在，无法获取实时状态";
                    MsgBean<GetGroupDeviceInfo> msgBean = MsgBean.getFalseInstance(errormsg);
                    out.print(msgBean.toJsonString());
                    out.flush();
                    out.close();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }
    }


    /**
     * @Author:xulei
     * @Description: APP接口，扫码添加设备
     * @Date:2018-2-25
     */
    @RequestMapping("/device/add_device_qrcode")
    @ResponseBody
    public void jsonPduQrcodeAdd(Model model, HttpServletRequest request, HttpServletResponse response, String qrcode) throws Exception {

        JSONObject json = null;

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println(df.format(System.currentTimeMillis()));
        PrintWriter out = response.getWriter();
        json = new JSONObject();
        JSONObject data = new JSONObject();
        qrcode = request.getParameter("qrcode");
        String group_id = request.getParameter("group_id");
        String access_token = request.getHeader("access_token");
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            try {

                String device_name = request.getParameter("device_name");
//            pdu = pduService.selectByMachineID(qrcode);
                if (qrcode.equals("") || qrcode.equals("null")) {//如果扫码解析的二维码为空 则返回错误

                    String errormsg = "二维码读取错误，尝试手动输入编号";
                    MsgBean msgBean = MsgBean.getFalseInstance(errormsg);
                    out.print(msgBean.toJsonString());
                    out.flush();
                    out.close();

                } else {

                    pdu = pduService.selectByQrcode(qrcode);
                    String pduMachineID = "";
                    if (pdu != null) {
                        String pdustate = pdu.getState();
                        if (pdustate.equals("1")) {//设备已存在并且状态正常
                            String errormsg = "设备已存在,请勿重复添加!";
                            MsgBean msgBean = MsgBean.getFalseInstance(errormsg);
                            out.print(msgBean.toJsonString());
                            out.flush();
                            out.close();

                        } else {//设备已存在，但是状态是删除
                            pduMachineID = pdu.getMachineid().toString() == null ? "" : pdu.getMachineid().toString();
                            pdu.setOnlinestate("0");
                            pdu.setActionState("0");
                            pduService.updateStateView(pdu);//修改设备表状态为1 正常
                            pduTemporaryService.updateStateAdd(pduMachineID); //修改临时设备表状态为1 已添加
                            //添加日志
                            LogUtil.addLog(token.getUsername(), "扫码添加设备：" + device_name);

                            MsgBean msgBean = MsgBean.getInstance();
                            out.print(msgBean.toJsonString());
                            out.flush();
                            out.close();
                        }
                    } else {//该设备存在于临时表中（之前建立过联系），但是没有添加到pdu表管理

                        //根据机器machineID 是否在临时表中有记录
                        PduTemporary pduTemporary = null;
                        pduTemporary = pduTemporaryService.selectByPduTemporary(qrcode);

                        if (pduTemporary != null) {//如果在设备临时表已有记录，则修改状态为1添加到pdu表
                            //插入到将新设备插入到pdu表中
                            Pdu pudNew = new Pdu();
                            pudNew.setMachineid(pduTemporary.getMachineid().toString() != "" ? pduTemporary.getMachineid().toString() : "null");
                            pudNew.setQrcode(pduTemporary.getQrcode().toString() != "" ? pduTemporary.getQrcode().toString() : "null");
                            pudNew.setIp(pduTemporary.getIp().toString() != "" ? pduTemporary.getIp().toString() : "null");
                            pudNew.setType(pduTemporary.getType().toString() != "" ? pduTemporary.getType().toString() : "null");
                            pudNew.setName(device_name);
                            pudNew.setState("1");
                            pudNew.setOnlinestate("0");
                            pudNew.setActionState("0");
                            pudNew.setCreateTime(df.format(System.currentTimeMillis()));
                            pduService.insert(pudNew);
                            pduTemporaryService.updateStateAdd(qrcode);


                            //添加到默认分组1
                            pdu = pduService.selectByQrcode(pudNew.getMachineid());
                            PduGroupRelation pduGroupRelation = new PduGroupRelation();
                            pduGroupRelation.setPdugroupid(1);
                            pduGroupRelation.setPduid(pdu.getId());
                            pduGroupRelationService.insert(pduGroupRelation);//添加分组关系
                            //添加日志
                            LogUtil.addLog(token.getUsername(), "添加设备：" + device_name + "添加到默认分组");

                            MsgBean msgBean = MsgBean.getInstance();
                            out.print(msgBean.toJsonString());
                            out.flush();
                            out.close();

                        } else {//完全是新设备，没有在temp表和pdu表存在记录，则一次性添加管理，也就是两个表都添加记录
                            PduTemporary pduTemporaryNew = new PduTemporary();

                            pduTemporaryNew.setMachineid(qrcode);
                            pduTemporaryNew.setIp("");

                            String pdutype = "";
                            if (qrcode.length() > 4) {
                                if (pdutype.substring(0, 3).equals("180")) {//空开设备只取前三位作为类型
                                    pdutype = qrcode.substring(0, 3);
                                } else {
                                    pdutype = qrcode.substring(0, 4);//插座和其他设备取前四位作为设备类型
                                }
                            } else {
                                pdutype = qrcode;
                            }
                            pduTemporaryNew.setType(pdutype);
                            pduTemporaryNew.setState("1");
                            pduTemporaryService.insert(pduTemporaryNew);//临时设备表添加记录

                            Pdu pduTwo = new Pdu();
                            pduTwo.setMachineid(qrcode);
                            pduTwo.setIp("");
                            pduTwo.setType(pdutype);
                            pduTwo.setState("1");
                            pduTwo.setCreateTime(df.format(System.currentTimeMillis()));
                            pduTwo.setName(device_name);
                            pduTwo.setActionState("0");
                            pduTwo.setOnlinestate("0");
                            pduService.insert(pduTwo);//pdu设备表添加记录

                            //添加到默认分组1
                            pdu = pduService.selectByQrcode(qrcode);
                            PduGroupRelation pduGroupRelation = new PduGroupRelation();
                            pduGroupRelation.setPdugroupid(1);
                            pduGroupRelation.setPduid(pdu.getId());
                            pduGroupRelationService.insertNew(pduGroupRelation);//添加分组关系
                            //添加日志
                            LogUtil.addLog(token.getUsername(), "添加设备：" + device_name + "到默认分组");
                            MsgBean msgBean = MsgBean.getInstance();
                            out.print(msgBean.toJsonString());
                            out.flush();
                            out.close();

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }

    }

    /**
     * @Author:xulei
     * @Description: APP接口，批量添加设备接口
     * @Date:2018-2-26
     */
    @RequestMapping("/device/add_device_to_group")
    @ResponseBody
    public void jsonPduListAdd(HttpServletRequest request, HttpServletResponse response, String obj1) throws Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String access_token = request.getHeader("access_token");
        String group_id = request.getParameter("group_id");
        String device_list = request.getParameter("device_list");
        String device_id = "";
        Gson gson = new Gson();
        DeviceToGroup deviceToGroup = gson.fromJson(device_list, DeviceToGroup.class);
        List<DeviceToGroup.DeviceListBean> list = deviceToGroup.getDevice_list();
        PrintWriter out = response.getWriter();
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            String s = "";
            for (int i = 0; i < list.size(); i++) {//解析获取设备ID
//            String device_id = jsonArray.getJSONObject(i).getString("device_id");
                DeviceToGroup.DeviceListBean deviceListBean = new DeviceToGroup.DeviceListBean();
                deviceListBean = list.get(i);
                if (s.equals("")) {
                    s += deviceListBean.getDevice_id();
                } else {
                    s += "," + deviceListBean.getDevice_id();
                }

            }

            String[] pduTempIDLsit = s.split(",");
            String pduTempID = "";
            String pduMachineID = "";

            String errorID = "";

            for (int i = 0; i < pduTempIDLsit.length; i++) {

                pduTempID = pduTempIDLsit[i].toString();
                PduTemporary pduTemporary = new PduTemporary();

                pduTemporary = pduTemporaryService.selectByPrimaryKey(Integer.valueOf(pduTempID));

                if (pduTemporary != null) {
                    pduMachineID = pduTemporary.getMachineid();

                    pdu = pduService.selectByMachineID(pduTempID);

                    PduGroupRelation pduGroupRelation = new PduGroupRelation();
                    pduGroupRelation.setPduid(pdu.getId());
                    pduGroupRelation.setPdugroupid(1);


                    if (pdu != null) {
                        pduService.updateStateView(pdu);//修改设备表状态为1 正常
                        pduTemporaryService.updateStateAdd(pduMachineID); //修改临时设备表状态为1 已添加

                        pduGroupRelationService.updateGroup(pduGroupRelation);//修改设备分组信息

                    } else {//该设备没有添加过，作为新设备进行添加管理

                        //根据机器machineID 查询出设备其他的信息；
                        PduTemporary pduTemporaryTwo = new PduTemporary();

                        pduTemporaryTwo = pduTemporaryService.selectByPduTemporary(pduMachineID);

                        if (pduTemporary != null) {
                            //插入到将新设备插入到pdu表中
                            Pdu pudNew = new Pdu();
                            pudNew.setMachineid(pduTemporary.getMachineid().toString() != "" ? pduTemporary.getMachineid().toString() : "null");
                            pudNew.setQrcode(pduTemporary.getQrcode().toString() != "" ? pduTemporary.getQrcode().toString() : "null");
                            pudNew.setIp(pduTemporary.getIp().toString() != "" ? pduTemporary.getIp().toString() : "null");
                            pudNew.setType(pduTemporary.getType().toString() != "" ? pduTemporary.getType().toString() : "null");
                            pudNew.setName(pduTemporary.getMachineid().toString() != "" ? pduTemporary.getMachineid().toString() : "null");
                            pudNew.setState("1");
                            pudNew.setCreateTime(df.format(System.currentTimeMillis()));
                            pduService.insert(pudNew);
                            pduTemporaryService.updateStateAdd(pduMachineID);
                            pduGroupRelationService.insert(pduGroupRelation);//添加分组信息

                        }
                    }
                } else {
                    errorID += pduTempID + ";";

                }
            }

            if (errorID.equals("")) {

                MsgBean msgBean = MsgBean.getInstance();
                out.print(msgBean.toJsonString());
                out.flush();
                out.close();

            } else {
                String msg = "设备ID：" + errorID + " 不存在，添加失败！";
                MsgBean msgBean = MsgBean.getFalseInstance(msg);
                out.print(msgBean.toJsonString());
                out.flush();
                out.close();
            }
        } else {
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }
    }


    /**
     * @Author:xulei
     * @Description:APP修改设备分组
     * @Date:2018-03-2
     */
    @RequestMapping("/device/grouping_device")
    @ResponseBody
    public void jsonGroupUpdate(HttpServletRequest request, HttpServletResponse response, String obj1) throws Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String access_token = request.getHeader("access_token");
        String group_id = request.getParameter("group_id");
        String device_list = request.getParameter("device_list");
        PrintWriter out = response.getWriter();
        String device_id = "";
        Gson gson = new Gson();
        DeviceToGroup deviceToGroup = gson.fromJson(device_list, DeviceToGroup.class);
        List<DeviceToGroup.DeviceListBean> list = deviceToGroup.getDevice_list();

        String s = "";

        String errorID = null;
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            try {
                for (int i = 0; i < list.size(); i++) {//解析获取设备ID
                    DeviceToGroup.DeviceListBean deviceListBean = new DeviceToGroup.DeviceListBean();
                    deviceListBean = list.get(i);
                    if (s.equals("")) {
                        s += deviceListBean.getDevice_id();
                    } else {
                        s += "," + deviceListBean.getDevice_id();
                    }

                }

                String[] pduTempIDLsit = s.split(",");
                String pduID = "";
                errorID = "";

                for (int i = 0; i < pduTempIDLsit.length; i++) {

                    pduID = pduTempIDLsit[i].toString();


                    PduGroupRelation pduGroupRelation = new PduGroupRelation();

                    pduGroupRelation = pduGroupRelationService.selectPdubyPdu(Integer.valueOf(pduID));
                    if (pduGroupRelation != null) {

                        pduGroupRelation.setPduid(Integer.valueOf(pduID));
                        pduGroupRelation.setPdugroupid(Integer.valueOf(group_id));
                        pduGroupRelationService.updateGroup(pduGroupRelation);
                        //添加日志
                        PduGroup group = pduGroupService.findPduGroupById(Integer.valueOf(group_id));
                        Pdu pdu = pduService.selectByPrimaryKey(Integer.valueOf(pduID));
                        LogUtil.addLog(token.getUsername(), "添加设备：" + pdu.getName() + "添加到" + group.getGroupname());

                    } else {
                        errorID += pduID + ";";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (errorID.equals("")) {

                MsgBean msgBean = MsgBean.getInstance();
                out.print(msgBean.toJsonString());
                out.flush();
                out.close();

            } else {
                String msg = "设备ID：" + errorID + " 不存在，修改失败！";
                MsgBean msgBean = MsgBean.getFalseInstance(msg);
                out.print(msgBean.toJsonString());
                out.flush();
                out.close();
            }
        } else {
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }
    }


    /**
     * @Author:xulei
     * @Description:APP设备删除接口
     * @Date: 2018-03-09
     */
    @RequestMapping("/device/delete_device")
    @ResponseBody
    public void jsonPduDelete(HttpServletRequest request, HttpServletResponse response, String id) throws
            Exception {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String access_token = request.getHeader("access_token");
        String device_list = request.getParameter("device_list");
        PrintWriter out = response.getWriter();
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            Gson gson = new Gson();
            DeviceToGroup deviceToGroup = gson.fromJson(device_list, DeviceToGroup.class);
            List<DeviceToGroup.DeviceListBean> list = deviceToGroup.getDevice_list();

            String s = "";
            for (int i = 0; i < list.size(); i++) {//解析获取设备ID
                DeviceToGroup.DeviceListBean deviceListBean = new DeviceToGroup.DeviceListBean();
                deviceListBean = list.get(i);
                if (s.equals("")) {
                    s += deviceListBean.getDevice_id();
                } else {
                    s += "," + deviceListBean.getDevice_id();
                }

            }

            String[] pduIDLsit = s.split(",");
            String pduID = "";
            out = response.getWriter();
            String errorID = "";

            for (int y = 0; y < pduIDLsit.length; y++) {

                pduID = pduIDLsit[y].toString();
                int ID = Integer.valueOf(pduID);
                pdu = pduService.selectByPrimaryKey(ID);

                if (pdu != null) {

                    //拉闸
                    PduController.pduOffAction(String.valueOf(pdu.getId()));

                    //修改状态
                    pduService.delete(pdu);
                    pduTemporaryService.delete(pdu.getMachineid().toString());
                    //添加日志
                    LogUtil.addLog(token.getUsername(), "删除设备：" + pdu.getName());
                } else {
                    errorID += pduID + ";";
                }
            }

            if (errorID.equals("")) {

                MsgBean msgBean = MsgBean.getInstance();
                msgBean.setData(new Object());
                out.print(msgBean.toJsonString());
                out.flush();
                out.close();

            } else {
                MsgBean msgBean = MsgBean.getFalseInstance("设备ID：" + errorID + " 不存在，删除失败！");
                out.print(msgBean.toJsonString());
                out.flush();
                out.close();
            }
        } else {
            MsgBean<GetGroupDeviceInfo> msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }


    }


    /**
     * @Author:xulei
     * @Description:APP设备搜索接口
     * @Date: 2018-03-09
     */
    @RequestMapping("/device/search_device")
    @ResponseBody
    public void jsonPdusearchDevice(HttpServletRequest request, HttpServletResponse response, String id) throws
            Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String access_token = request.getHeader("access_token");
        String keyword = request.getParameter("keyword");
        PrintWriter out = response.getWriter();
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {
            Map map = new HashMap();
            map.put("keyword", keyword);
//        PduInfoTemp pduInfoTemp = new PduInfoTemp();
            List<PduInfoTemp> pduInfoTempList = new ArrayList<PduInfoTemp>();
            pduInfoTempList = pduService.selectByPduKeyword(map);

            GetGroupDeviceInfo getGroupDeviceInfo = new GetGroupDeviceInfo();
            List<GetGroupDeviceInfo.GroupDeviceInfoBean> devicelist = new ArrayList<GetGroupDeviceInfo.GroupDeviceInfoBean>();


//        int ID = Integer.valueOf(device_id);
//        PduInfoTemp pduInfoTemp = new PduInfoTemp();
//        pduInfoTemp = pduService.pduInfoByPrimaryKey(ID);

            if (pduInfoTempList.size() > 0) {

                for (int i = 0; i < pduInfoTempList.size(); i++) {
                    GetGroupDeviceInfo.GroupDeviceInfoBean DeviceInfo = new GetGroupDeviceInfo.GroupDeviceInfoBean();
                    PduInfoTemp pduInfoTemp = new PduInfoTemp();
                    pduInfoTemp = pduInfoTempList.get(i);
                    DeviceInfo.setDevice_id(pduInfoTemp.getPduid().toString());
                    DeviceInfo.setDevice_name(pduInfoTemp.getName() == null ? "" : pduInfoTemp.getName());
                    DeviceInfo.setDevice_state(Integer.valueOf(pduInfoTemp.getOnlinestate() == null ? "1" : pduInfoTemp.getOnlinestate()));
                    DeviceInfo.setGroup_id(String.valueOf(pduInfoTemp.getPdugroupid() == null ? "1" : pduInfoTemp.getPdugroupid()));
                    DeviceInfo.setGroup_name(pduInfoTemp.getGroupname() == null ? "" : pduInfoTemp.getGroupname());
                    DeviceInfo.setPower(Float.parseFloat(pduInfoTemp.getWatt() == null ? "0.0" : pduInfoTemp.getWatt()));
                    DeviceInfo.setElectricity(Float.parseFloat(pduInfoTemp.getCurrent() == null ? "0.0" : pduInfoTemp.getCurrent()));
                    DeviceInfo.setVoltage(Float.parseFloat(pduInfoTemp.getVoltage() == null ? "0.0" : pduInfoTemp.getVoltage()));
                    DeviceInfo.setOvervoltage(Integer.valueOf(pduInfoTemp.getOvervoltage() == null ? "0" : pduInfoTemp.getOvervoltage()));
                    DeviceInfo.setUndervoltage(Integer.valueOf(pduInfoTemp.getUndervoltage() == null ? "0" : pduInfoTemp.getUndervoltage()));
                    DeviceInfo.setOvercurrent(Integer.valueOf(pduInfoTemp.getOvercurrent() == null ? "0" : pduInfoTemp.getOvercurrent()));
                    DeviceInfo.setOpen_circuit(Integer.valueOf(pduInfoTemp.getCircuitbreaker() == null ? "0" : pduInfoTemp.getCircuitbreaker()));
                    DeviceInfo.setElectric_leakage(Integer.valueOf(pduInfoTemp.getElectricleakage() == null ? "0" : pduInfoTemp.getElectricleakage()));
                    DeviceInfo.setRelay(Integer.valueOf(pduInfoTemp.getRelaystate() == null ? "1" : pduInfoTemp.getRelaystate()));
                    DeviceInfo.setIp(pduInfoTemp.getIp() == null ? "" : pduInfoTemp.getIp());
                    DeviceInfo.setMachine_id(pduInfoTemp.getMachineid() == null ? "" : pduInfoTemp.getMachineid());
                    DeviceInfo.setUptime(StringUtil.convertTimeToLong(pduInfoTemp.getUpdateTime() == null ? "0" : pduInfoTemp.getUpdateTime()));//可能需要格式化或修改类型

                    devicelist.add(DeviceInfo);
                }


                getGroupDeviceInfo.setDevice_list(devicelist);

                MsgBean<GetGroupDeviceInfo> msgBean = MsgBean.getInstance();
                msgBean.setData(getGroupDeviceInfo);

                out.print(msgBean.toJsonString());
                out.flush();
                out.close();

            } else {
                MsgBean<GetGroupDeviceInfo> msgBean = MsgBean.getFalseInstance("没有搜索到符合条件的设备！");
                out.print(msgBean.toJsonString());
                out.flush();
                out.close();

            }
        } else {
            MsgBean<GetGroupDeviceInfo> msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }


    }

    /**
     * @Author: kanyuanfeng
     * @Date: 2018/5/2
     * @Description:app新增单个定时任务接口
     */
    @RequestMapping("/timing/new_switch")
    @ResponseBody
    public void jsonPduTimingNewSwitch(HttpServletRequest request, HttpServletResponse response) throws
            Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String access_token = request.getHeader("access_token");
        String mode = request.getParameter("mode");
        String date = request.getParameter("date");//日期
        String time = request.getParameter("time");//时间
        String repeat_day = request.getParameter("repeat_day");
        String action = request.getParameter("action");
        String state = request.getParameter("state");
        String device_id = request.getParameter("device_id");
        PrintWriter out = response.getWriter();
        MsgBean msgBean = null;
        try {
            //验证并解析token
            Token token = TokenUtil.apptokenyanzheng(access_token);
            if (token.getJieguo()) {
                PduClock pduClock = new PduClock();
                pduClock.setPduid(Integer.valueOf(device_id));
                pduClock.setMode(mode);
                pduClock.setActiontime("");
                pduClock.setAction(action);
                pduClock.setRepeatday(mode.equals("0") ? "" : repeat_day);
                pduClock.setClockdate(date);
                pduClock.setClocktime(time);
                pduClock.setResultstate(state);
                pduClockService.insertSelective(pduClock);
                Map<String, Object> map = new HashMap();
                map.put("task_id", String.valueOf(pduClock.getId()));
                map.put("mode", Integer.valueOf(mode));
                map.put("date", date);
                map.put("time", time);
                List<Integer> list = new ArrayList();
                if (null != repeat_day && !"[]".equals(repeat_day)) {
                    String[] repeat = repeat_day.substring(1, repeat_day.length() - 1).split(", ");
                    for (int y = 0; y < repeat.length; y++) {
                        list.add(Integer.valueOf(repeat[y]));
                    }
                }

                //添加日志
                LogUtil.addLog(token.getUsername(), "新增定时任务");
                map.put("repeat_day", mode.equals("0") ? new ArrayList() : list);
                map.put("action", Integer.valueOf(action));
                map.put("state", Integer.valueOf(state));
                msgBean = MsgBean.getInstance();
                msgBean.setData(map);
                //添加日志
                //0星期天，1星期1...6星期6
                String logAction = this.getLogAction(mode, action, date, time, repeat_day);
                Pdu pdu = pduService.selectByPrimaryKey(Integer.valueOf(device_id));
                LogUtil.addLog(token.getUsername(), "新增定时任务：" + logAction + pdu.getName());

                //设置设备定时任务线程  确保在没有网络或服务器没有发出指令的情况下，设备自己能够开启或关闭
//                Pdu clockPdu = new Pdu();
//                clockPdu = pduService.selectByPrimaryKey(pduClock.getPduid());
//                String ip = clockPdu.getIp();
//                Socket connection = null;
//                connection =  (Socket) BaseController.SubPolmap.get(ip); //获取socket连接对象
//
//                Thread pduOnOffthread = new Thread(new PduClockSetThread(connection, pduClock,clockPdu.getMachineid()));
//                pduOnOffthread.start();
//                pduOnOffthread.join();


            } else {
                msgBean = MsgBean.getTokenFalseInstance();
            }
        } catch (Exception e) {
            msgBean = MsgBean.getFalseInstance("新增定时任务失败");
            e.printStackTrace();
        }

        out.print(msgBean.toJsonString());
        out.flush();
        out.close();
    }

    /**
     * @Author:徐磊
     * @Description:app批量新增定时任务接口
     * @Date: 2018-04-03
     */
    @RequestMapping("/timing/new_multiple_switch")
    @ResponseBody
    public void jsonPduTimingNewmultipleSwitch(HttpServletRequest request, HttpServletResponse response) throws
            Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String access_token = request.getHeader("access_token");
        String mode = request.getParameter("mode");
        String date = request.getParameter("date");//日期
        String time = request.getParameter("time");//时间
        String repeat_day = request.getParameter("repeat_day");
        String action = request.getParameter("action");
        String state = request.getParameter("state");
        String device_list = request.getParameter("device_list");
        String[] pduids = device_list.substring(1, device_list.length() - 1).split(", ");
        PrintWriter out = response.getWriter();
        MsgBean msgBean = null;
        try {
            //验证并解析token
            Token token = TokenUtil.apptokenyanzheng(access_token);
            if (token.getJieguo()) {
                for (int i = 0; i < pduids.length; i++) {
                    PduClock pduClock = new PduClock();
                    pduClock.setPduid(Integer.valueOf(pduids[i]));
                    if (mode.equals("0")) {//指定日期执行定时任务
                        pduClock.setMode(mode);
                        pduClock.setActiontime("");
                        pduClock.setAction(action);
                        pduClock.setRepeatday("");
                        pduClock.setClockdate(date);
                        pduClock.setClocktime(time);
                        pduClock.setResultstate(state);
                        pduClockService.insertSelective(pduClock);
                    }
                    if (mode.equals("1")) {//模式为1 按照星期一至星期日重复执行定时任务
                        pduClock.setMode(mode);
                        pduClock.setActiontime("");
                        pduClock.setAction(action);
                        pduClock.setRepeatday(repeat_day);
                        pduClock.setClockdate(date);
                        pduClock.setClocktime(time);
                        pduClock.setResultstate(state);
                        pduClockService.insertSelective(pduClock);
                    }
                    //添加日志
                    String logAction = this.getLogAction(mode, action, date, time, repeat_day);
                    Pdu pdu = pduService.selectByPrimaryKey(Integer.valueOf(pduids[i]));
                    LogUtil.addLog(token.getUsername(), "新增定时任务：" + logAction + pdu.getName());
                }
                msgBean = MsgBean.getInstance();
            } else {
                msgBean = MsgBean.getTokenFalseInstance();
            }
        } catch (NumberFormatException e) {
            msgBean = MsgBean.getFalseInstance("新增定时任务失败");
            e.printStackTrace();
        }

        out.print(msgBean.toJsonString());
        out.flush();
        out.close();
    }


    /**
     * @Author:徐磊
     * @Description:app修改定时任务接口
     * @Date: 2018-04-03
     */
    @RequestMapping("/timing/modify_switch")
    @ResponseBody
    public void jsonPduTimingModifySwitch(HttpServletRequest request, HttpServletResponse response) throws
            Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String task_id = request.getParameter("task_id");
        String device_id = request.getParameter("device_id");
        String mode = request.getParameter("mode");
        String date = request.getParameter("date");//日期
        String time = request.getParameter("time");//时间
        String repeat_day = request.getParameter("repeat_day");
        String action = request.getParameter("action");
        String state = request.getParameter("state");
        String access_token = request.getHeader("access_token");
        PrintWriter out = response.getWriter();
        MsgBean msgBean = null;
        //  PduClock pduClock = new PduClock();
        //pduClock.setId(Integer.valueOf(task_id));

        try {
            //验证并解析token
            Token token = TokenUtil.apptokenyanzheng(access_token);
            if (token.getJieguo()) {
                //根据id查询pduClock
                PduClock pduClock = pduClockService.selectByPrimaryKey(Integer.valueOf(task_id));
                pduClock.setPduid(Integer.valueOf(device_id));
                if (mode.equals("0")) {//指定日期执行定时任务
                    pduClock.setMode(mode);
                    pduClock.setActiontime("");
                    pduClock.setAction(action);
                    pduClock.setRepeatday("");
                    pduClock.setClockdate(date);
                    pduClock.setClocktime(time);
                    pduClock.setResultstate(state);
                    pduClockService.updateByPrimaryKeySelective(pduClock);
                }
                if (mode.equals("1")) {//模式为1 按照星期一至星期日重复执行定时任务
                    pduClock.setMode(mode);
                    pduClock.setActiontime("");
                    pduClock.setAction(action);
                    pduClock.setRepeatday(repeat_day);
                    pduClock.setClockdate(date);
                    pduClock.setClocktime(time);
                    pduClock.setResultstate(state);
                    pduClockService.updateByPrimaryKeySelective(pduClock);

                }
           /* pduClock.setPduid(Integer.valueOf(device_id));
            pduClock.setMode(mode);
            pduClock.setActiontime("");
            pduClock.setAction(action);
            repeat_day = repeat_day.replace("[", "").replace("]", "");
            pduClock.setRepeatday(repeat_day);
            pduClock.setClockdate(date);
            pduClock.setClocktime(time + ":00");
            pduClock.setResultstate(state);
            pduClockService.updateByPrimaryKeySelective(pduClock);*/
                msgBean = MsgBean.getInstance();
                //添加日志
                String logAction = this.getLogAction(mode, action, date, time, repeat_day);
                Pdu pdu = pduService.selectByPrimaryKey(Integer.valueOf(device_id));
                LogUtil.addLog(token.getUsername(), "修改定时任务：" + logAction + pdu.getName());
            } else {
                msgBean = MsgBean.getTokenFalseInstance();
            }
        } catch (NumberFormatException e) {
            msgBean = MsgBean.getFalseInstance("修改定时任务失败");
            e.printStackTrace();
        }
        out.print(msgBean.toJsonString());
        out.flush();
        out.close();

    }

    /**
     * @Author:xulei
     * @Description:APP删除定时任务
     * @Date:2018-04-03
     */
    @RequestMapping("/timing/delete_task")
    @ResponseBody
    public void jsonPduTimingDeletTask(HttpServletRequest request, HttpServletResponse response) throws
            Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String access_token = request.getHeader("access_token");
        String task_id = request.getParameter("task_id");
        PrintWriter out = response.getWriter();
        MsgBean msgBean = null;
        try {
            //验证并解析token
            Token token = TokenUtil.apptokenyanzheng(access_token);
            if (token.getJieguo()) {
                Integer pduid = pduClockService.selectByPrimaryKey(Integer.valueOf(task_id)).getPduid();
                pduClockService.deleteByPrimaryKey(Integer.valueOf(task_id));
                msgBean = MsgBean.getInstance();
                //添加日志
                Pdu pdu = pduService.selectByPrimaryKey(pduid);
                LogUtil.addLog(token.getUsername(), "删除设备" + pdu.getName() + "的定时任务");
            } else {
                msgBean = MsgBean.getTokenFalseInstance();
            }
        } catch (NumberFormatException e) {
            msgBean = MsgBean.getFalseInstance("删除定时任务失败");
            e.printStackTrace();
        }
        out.print(msgBean.toJsonString());
        out.flush();
        out.close();

    }


    /**
     * @Author:xulei
     * @Description:app获取设备的定时任务列表
     * @Date: 2018-04-03
     */
    @RequestMapping("/timing/get_timing_list")
    @ResponseBody
    public void jsonPduTimingGetTimingList(HttpServletRequest request, HttpServletResponse response) throws
            Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String access_token = request.getHeader("access_token");
        String device_id = request.getParameter("device_id");
        PrintWriter out = response.getWriter();
        MsgBean msgBean = null;
        try {
            //验证并解析token
            Token token = TokenUtil.apptokenyanzheng(access_token);
            if (token.getJieguo()) {
                //PduClock pduClock = new PduClock();
                List<PduClock> pduClockList = pduClockService.selectPduClockByPduId(Integer.valueOf(device_id));
                GetTimingListBean getTimingListBean = new GetTimingListBean();
                List<GetTimingListBean.TimingTaskListBean> timing_task_list = new ArrayList<GetTimingListBean.TimingTaskListBean>();
                for (int i = 0; i < pduClockList.size(); i++) {
                    PduClock pduClock = new PduClock();
                    pduClock = pduClockList.get(i);

                    GetTimingListBean.TimingTaskListBean timingTaskListBean = new GetTimingListBean.TimingTaskListBean();
                    timingTaskListBean.setTask_id(pduClock.getId());
                    timingTaskListBean.setMode(Integer.valueOf(pduClock.getMode()));
                    //String date = pduClock.getActiontime().substring(0, 10);
                    //String time = pduClock.getActiontime().substring(12, 16);
                    //timingTaskListBean.setDate(date);
                    //timingTaskListBean.setTime(time);
                    timingTaskListBean.setDate(pduClock.getClockdate());
                    timingTaskListBean.setTime(pduClock.getClocktime());
                    //拼接重复日期
                    String repeatday = pduClock.getRepeatday();
                    List<Integer> list = new ArrayList();
                    if (null != repeatday && !"".equals(repeatday)) {
                        String[] repeat = repeatday.substring(1, repeatday.length() - 1).split(", ");
                        for (int y = 0; y < repeat.length; y++) {
                            list.add(Integer.valueOf(repeat[y]));

                        }
                    }
                    timingTaskListBean.setRepeat_day(list);
                    timingTaskListBean.setAction(Integer.valueOf(pduClock.getAction()));
                    timingTaskListBean.setState(Integer.valueOf(pduClock.getResultstate()));

                    timing_task_list.add(timingTaskListBean);
                }

                getTimingListBean.setTiming_task_list(timing_task_list);
                msgBean = MsgBean.getInstance();
                msgBean.setData(getTimingListBean);
            } else {
                msgBean = MsgBean.getTokenFalseInstance();
            }
        } catch (NumberFormatException e) {
            msgBean = MsgBean.getFalseInstance("获取定时任务列表失败");
            e.printStackTrace();
        }
        out.print(msgBean.toJsonString());
        out.flush();
        out.close();

    }


    /**
     * @Author:xulei
     * @Description:app修改设备名称
     * @Date: 2018-04-18
     */
    @RequestMapping("/device/modify_name")
    @ResponseBody
    public void jsonPduModifyName(HttpServletRequest request, HttpServletResponse response) throws
            Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String device_id = request.getParameter("device_id");
        String device_name = request.getParameter("device_name");
        String access_token = request.getHeader("access_token");
        PrintWriter out = response.getWriter();
        MsgBean msgBean = null;
        try {
            //验证并解析token
            Token token = TokenUtil.apptokenyanzheng(access_token);
            if (token.getJieguo()) {
                Pdu pdu = pduService.selectByPrimaryKey(Integer.valueOf(device_id));
                this.pdu.setId(Integer.valueOf(device_id));
                this.pdu.setName(device_name);
                pduService.update(this.pdu);
                //添加日志
                LogUtil.addLog(token.getUsername(), "修改" + pdu.getName() + "名称：" + device_name);
                msgBean = MsgBean.getInstance();
            } else {
                msgBean = MsgBean.getTokenFalseInstance();
            }
        } catch (Exception e) {
            msgBean = MsgBean.getFalseInstance("修改设备名称失败");
            e.printStackTrace();
        }
        msgBean.setData(msgBean.toJsonString());
        out.print(msgBean.toJsonString());
        out.flush();
        out.close();

    }


    /**
     * @Author:xulei
     * @Description:app修改设备IP
     * @Date: 2018-04-18
     */
    @RequestMapping("/device/modify_ip")
    @ResponseBody
    public void jsonPduModifyIp(HttpServletRequest request, HttpServletResponse response) throws
            Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String device_id = request.getParameter("device_id");
        String ip = request.getParameter("ip");
        PrintWriter out = response.getWriter();
        String access_token = request.getHeader("access_token");
        MsgBean msgBean = null;
        try {
            //验证并解析token
            Token token = TokenUtil.apptokenyanzheng(access_token);
            if (token.getJieguo()) {
                Pdu pdu = pduService.selectByPrimaryKey(Integer.valueOf(device_id));
                this.pdu.setId(Integer.valueOf(device_id));
                this.pdu.setIp(ip);
                pduService.update(this.pdu);
                //添加日志
                LogUtil.addLog(token.getUsername(), "修改" + pdu.getName() + "IP：" + ip);
                msgBean = MsgBean.getInstance();
            } else {
                msgBean = MsgBean.getTokenFalseInstance();
            }
        } catch (Exception e) {
            msgBean = MsgBean.getFalseInstance("修改失败");
            e.printStackTrace();
        }
        msgBean.setData(msgBean.toJsonString());
        out.print(msgBean.toJsonString());
        out.flush();
        out.close();

    }

    /**
     * @Author: kanyuanfeng
     * @Date: 2018/5/8
     * @Description:定时任务日志，时间装换
     */
    public String getLogAction(String mode, String action, String date, String time, String repeat_day) {
        String logAction = "0".equals(action) ? "关闭" : "打开";
        String actionTime = "";
        if ("0".equals(mode)) {//不重复
            actionTime = date + " ";
        } else {
            if (null != repeat_day && !"[]".equals(repeat_day)) {
                String[] repeat = repeat_day.substring(1, repeat_day.length() - 1).split(", ");
                for (int y = 0; y < repeat.length; y++) {
                    int logtime = Integer.valueOf(repeat[y]);
                    switch (logtime) {
                        case 0:
                            actionTime += "星期天 ";
                            break;
                        case 1:
                            actionTime += "星期一 ";
                            break;
                        case 2:
                            actionTime += "星期二 ";
                            break;
                        case 3:
                            actionTime += "星期三 ";
                            break;
                        case 4:
                            actionTime += "星期四 ";
                            break;
                        case 5:
                            actionTime += "星期五 ";
                            break;
                        case 6:
                            actionTime += "星期六 ";
                            break;
                    }
                }
            }
        }
        return actionTime + time + logAction;
    }


    /**
     * @Author:xulei
     * @Description:获取空开分组信息
     * @Date:2018-05-11
     */

    @RequestMapping("/device/get_device_relationship_list")
    @ResponseBody
    public void jsonPduRelationship(HttpServletRequest request, HttpServletResponse response) throws
            Exception {
//        MsgBean msgBean = null;
        PrintWriter out = null;
        GetDeviceRelationshipList GetDeviceRelationshipList = null;
        try {
            request.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=UTF-8");
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            out = response.getWriter();
            GetDeviceRelationshipList = new GetDeviceRelationshipList();

            //搜索所有的空开设备
            List<Pdu> switchList = new ArrayList<Pdu>();
            Pdu switchPdu = new Pdu();

            switchPdu.setType("180");
            switchList = pduService.selectAllBySearch(switchPdu);

            //保存空开列表集合
            List<GetDeviceRelationshipList.DeviceListBean> switchInPutList = new ArrayList<com.chuangkou.pdu.bean.GetDeviceRelationshipList.DeviceListBean>();

            for (int i = 0; i < switchList.size(); i++) {//遍历空开设备
                //根据空开设备ID 返回设备详情
                Pdu switchPduTwo = new Pdu();
                switchPduTwo = switchList.get(i);
                switchPduTwo.getId();

                GetDeviceRelationshipList.DeviceListBean switchBean = getSwitchPduBean(switchPduTwo.getId());

                List<GetDeviceRelationshipList.DeviceListBean.ChildrensBean> childrensBeanList = new ArrayList<com.chuangkou.pdu.bean.GetDeviceRelationshipList.DeviceListBean.ChildrensBean>();

                //根据空开设备搜索所有的关联的插座设备（星型拓扑）
                List<PduRelation> onePduRelationList = new ArrayList<PduRelation>();
                //parentID==null的 集合1
                onePduRelationList = pduRelationService.selectByPlugs(switchPduTwo.getId());

                List<PduRelation> twoPduRelationList = new ArrayList<PduRelation>();
                //parentID不等于空 并且switchpduid==当前空开的 集合2
                twoPduRelationList = pduRelationService.selectByPlugsNotEmpty(switchPduTwo.getId());

                List topTree = new ArrayList();

                for (int s = 0; s < onePduRelationList.size(); s++) {
                    PduRelation pduRelation = new PduRelation();
                    pduRelation = onePduRelationList.get(s);

                    GetDeviceRelationshipList.DeviceListBean.ChildrensBean oneChildrensBean = new GetDeviceRelationshipList.DeviceListBean.ChildrensBean();

                    oneChildrensBean = getOnechildrensBean(pduRelation.getPduID());

                    List<PduRelation> resultTree = new ArrayList<PduRelation>();
                    resultTree = getTreeList(pduRelation.getPduID(), twoPduRelationList);
                    System.out.println("获取插座的树形结果" + resultTree.size());

                    oneChildrensBean.setChildrens(resultTree);

                    childrensBeanList.add(oneChildrensBean);
                }
                switchBean.setChildrens(childrensBeanList);
                switchInPutList.add(switchBean);
            }
            GetDeviceRelationshipList.setDevice_list(switchInPutList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MsgBean<GetDeviceRelationshipList> msgBean = MsgBean.getInstance();
        msgBean.setData(GetDeviceRelationshipList);
        out.print(msgBean.toJsonString());
        out.flush();
        out.close();


    }


    /**
     * @Author:xulei
     * @Description:根据空开设备ID 搜索空开设备的info信息
     * @Date:2018-06-14
     */

    public GetDeviceRelationshipList.DeviceListBean getSwitchPduBean(int PduID) {

        PduInfoTemp pduInfoTemp = new PduInfoTemp();
        pduInfoTemp = pduService.pduInfoByPrimaryKey(PduID);

        GetDeviceRelationshipList.DeviceListBean deviceListBean = new GetDeviceRelationshipList.DeviceListBean();

        if (pduInfoTemp != null) {
            deviceListBean.setDevice_id(String.valueOf(pduInfoTemp.getPduid()));
            deviceListBean.setDevice_name(pduInfoTemp.getName());
            deviceListBean.setDevice_state(Integer.valueOf(pduInfoTemp.getOnlinestate()));
            deviceListBean.setGroup_id(String.valueOf(pduInfoTemp.getPdugroupid()));

            deviceListBean.setPower(Float.parseFloat(pduInfoTemp.getWatt() == null ? "0.0" : pduInfoTemp.getWatt()));
            deviceListBean.setElectricity(Float.parseFloat(pduInfoTemp.getCurrent() == null ? "0.0" : pduInfoTemp.getCurrent()));
            deviceListBean.setVoltage(Float.parseFloat(pduInfoTemp.getVoltage() == null ? "0.0" : pduInfoTemp.getVoltage()));


            deviceListBean.setElectronictages1(pduInfoTemp.getElectronictages1());
            deviceListBean.setElectronictages2(pduInfoTemp.getElectronictages2());
            deviceListBean.setElectronictages3(pduInfoTemp.getElectronictages3());
            deviceListBean.setElectronictages4(pduInfoTemp.getElectronictages4());
            deviceListBean.setElectronictages5(pduInfoTemp.getElectronictages5());
            deviceListBean.setElectronictages6(pduInfoTemp.getElectronictages6());

            String pdutype = pduInfoTemp.getType() == null ? "-1" : pduInfoTemp.getType();

            if (pdutype.equals("0000")) pdutype = "0";
            if (pdutype.equals("0001")) pdutype = "1";
            if (pdutype.equals("180")) pdutype = "2";
            deviceListBean.setPdutype(Integer.valueOf(pdutype));
            deviceListBean.setParent_id("");

        }
        return deviceListBean;
    }


    /**
     * @Author:xulei
     * @Description:一级插座信息生成
     * @Date: 2018-06-14
     */
    public GetDeviceRelationshipList.DeviceListBean.ChildrensBean getOnechildrensBean(int pduID) {

        PduInfoTemp pduInfoTempTwo = new PduInfoTemp();
        pduInfoTempTwo = pduService.pduInfoByPrimaryKey(pduID);

        GetDeviceRelationshipList.DeviceListBean.ChildrensBean childrensBean = new GetDeviceRelationshipList.DeviceListBean.ChildrensBean();

        if (pduInfoTempTwo != null) {
            childrensBean.setDevice_id(String.valueOf(pduInfoTempTwo.getPduid()));
            childrensBean.setDevice_name(pduInfoTempTwo.getName());
            childrensBean.setDevice_state(Integer.valueOf(pduInfoTempTwo.getOnlinestate()));
            childrensBean.setGroup_id(String.valueOf(pduInfoTempTwo.getPdugroupid()));
            childrensBean.setGroup_name(pduInfoTempTwo.getGroupname());
            childrensBean.setPower(Float.parseFloat(pduInfoTempTwo.getWatt() == null ? "0.0" : pduInfoTempTwo.getWatt()));
            childrensBean.setElectricity(Float.parseFloat(pduInfoTempTwo.getCurrent() == null ? "0.0" : pduInfoTempTwo.getCurrent()));
            childrensBean.setVoltage(Float.parseFloat(pduInfoTempTwo.getVoltage() == null ? "0.0" : pduInfoTempTwo.getVoltage()));

            String childrenPduType = pduInfoTempTwo.getType() == null ? "-1" : pduInfoTempTwo.getType();
            if (childrenPduType.equals("0000")) childrenPduType = "0";
            if (childrenPduType.equals("0001")) childrenPduType = "1";
            if (childrenPduType.equals("180")) childrenPduType = "2";
            childrensBean.setPdutype(Integer.valueOf(childrenPduType));
            PduRelation pduRelation = pduRelationService.selectByPrimaryKey(pduID);
            childrensBean.setParent_id(pduRelation.getSwitchPduID() == null ? "" : String.valueOf(pduRelation.getSwitchPduID()));

        } else {
            Pdu pdutwo = pduService.selectByPrimaryKey(pduID);
            childrensBean.setDevice_id(String.valueOf(pdutwo.getId()));
            childrensBean.setDevice_name(pdutwo.getName());
            childrensBean.setDevice_state(Integer.valueOf(pdutwo.getState()));

            String childrenPduType = pdutwo.getType() == null ? "-1" : pdutwo.getType();
            if (childrenPduType.equals("0000")) childrenPduType = "0";
            if (childrenPduType.equals("0001")) childrenPduType = "1";
            if (childrenPduType.equals("180")) childrenPduType = "2";
            childrensBean.setPdutype(Integer.valueOf(childrenPduType));

            PduRelation pduRelation = pduRelationService.selectByPrimaryKey(pduID);
            childrensBean.setParent_id(pduRelation.getSwitchPduID() == null ? "" : String.valueOf(pduRelation.getSwitchPduID()));

        }
        return childrensBean;
    }


    /**
     * @Author:xulei
     * @Description:多级插座的信息获取
     * @Date: 2018-06-14
     */
    public PduRelation getSubChildrensBean(int pduID) {

        PduInfoTemp pduInfoTempTwo = new PduInfoTemp();
        pduInfoTempTwo = pduService.pduInfoByPrimaryKey(pduID);

        PduRelation pduSubChildrensBean = new PduRelation();

        PduRelation pduRelation = pduRelationService.selectByPrimaryKey(pduID);

        if (pduInfoTempTwo != null) {
            pduSubChildrensBean.setDevice_id(String.valueOf(pduInfoTempTwo.getPduid()));
            pduSubChildrensBean.setDevice_name(pduInfoTempTwo.getName());
            pduSubChildrensBean.setDevice_state(Integer.valueOf(pduInfoTempTwo.getOnlinestate()));
            pduSubChildrensBean.setGroup_id(String.valueOf(pduInfoTempTwo.getPdugroupid()));
            pduSubChildrensBean.setGroup_name(pduInfoTempTwo.getGroupname());
            pduSubChildrensBean.setPower(Float.parseFloat(pduInfoTempTwo.getWatt() == null ? "0.0" : pduInfoTempTwo.getWatt()));
            pduSubChildrensBean.setElectricity(Float.parseFloat(pduInfoTempTwo.getCurrent() == null ? "0.0" : pduInfoTempTwo.getCurrent()));
            pduSubChildrensBean.setVoltage(Float.parseFloat(pduInfoTempTwo.getVoltage() == null ? "0.0" : pduInfoTempTwo.getVoltage()));

            String childrenPduType = pduInfoTempTwo.getType() == null ? "-1" : pduInfoTempTwo.getType();
            if (childrenPduType.equals("0000")) childrenPduType = "0";
            if (childrenPduType.equals("0001")) childrenPduType = "1";
            if (childrenPduType.equals("180")) childrenPduType = "2";
            pduSubChildrensBean.setPdutype(Integer.valueOf(childrenPduType));

            pduSubChildrensBean.setParent_id(pduRelation.getParent_id() == null ? "" : pduRelation.getParent_id());

        } else {
            if (pduRelation != null) {

//                PduRelation pduRelationTWO = pduRelationService.selectByPduInfo(pduID);
                Pdu pdutwo = pduService.selectByPrimaryKey(pduID);
                pduSubChildrensBean.setDevice_id(String.valueOf(pdutwo.getId()));
                pduSubChildrensBean.setDevice_name(pdutwo.getName());
                pduSubChildrensBean.setDevice_state(Integer.valueOf(pdutwo.getState()));
//                pduSubChildrensBean.setGroup_id(String.valueOf(pduInfoTempTwo.getPdugroupid()));
//                pduSubChildrensBean.setGroup_name(pduInfoTempTwo.getGroupname());
//                pduSubChildrensBean.setPower(Float.parseFloat(pduInfoTempTwo.getWatt() == null ? "0.0" : pduInfoTempTwo.getWatt()));
//                pduSubChildrensBean.setElectricity(Float.parseFloat(pduInfoTempTwo.getCurrent() == null ? "0.0" : pduInfoTempTwo.getCurrent()));
//                pduSubChildrensBean.setVoltage(Float.parseFloat(pduInfoTempTwo.getVoltage() == null ? "0.0" : pduInfoTempTwo.getVoltage()));
//
                String childrenPduType = pdutwo.getType() == null ? "-1" : pdutwo.getType();
                if (childrenPduType.equals("0000")) childrenPduType = "0";
                if (childrenPduType.equals("0001")) childrenPduType = "1";
                if (childrenPduType.equals("180")) childrenPduType = "2";
                pduSubChildrensBean.setPdutype(Integer.valueOf(childrenPduType));

                pduSubChildrensBean.setParent_id(pduRelation.getParent_id() == null ? "" : pduRelation.getParent_id());

            }


        }
        return pduSubChildrensBean;
    }

    /**
     * @Author:xulei
     * @Description:获取空开线路老化分析数据
     * @Date:2018-05-11
     */

    @RequestMapping("/device/get_resistance_history")
    @ResponseBody
    public void jsonPduResistance_history(HttpServletRequest request, HttpServletResponse response) throws
            Exception {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        String access_token = request.getHeader("access_token");
        String device_id = request.getParameter("device_id");
        String start_date = request.getParameter("start_date");
        String end_date = request.getParameter("end_date");
        String query_type = request.getParameter("query_type");

        start_date = start_date + " 00:00:00";
        end_date = end_date + " 23:59:59";
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        start_date = sdf.parse(start_date).toString();
//        end_date = sdf.parse(end_date).toString();
        int ID = Integer.valueOf(device_id);

        access_token = "admin,21232F297A57A5A743894A0E4A801FC3,1528014909555";
        //验证并解析token
        Token token = TokenUtil.apptokenyanzheng(access_token);
        if (token.getJieguo()) {

            try {
                if (query_type.equals("0")) {

                    PduOldLine pduOldLine = new PduOldLine();
                    pduOldLine.setPduID(ID);
                    pduOldLine.setStarttime(start_date);
                    pduOldLine.setEndtime(end_date);
                    List<PduOldLine> pduOldLineList = new ArrayList<PduOldLine>();

                    pduOldLineList = pduOldLineService.selectAllByHistoryData(pduOldLine);

                    GetResistanceHistory getResistanceHistory = new GetResistanceHistory();
                    List<GetResistanceHistory.HistoryListBean> powerList = new ArrayList<GetResistanceHistory.HistoryListBean>();

                    if (pduOldLineList.size() > 0) {

                        for (int i = 0; i < pduOldLineList.size(); i++) {
                            GetResistanceHistory.HistoryListBean historyListBean = new GetResistanceHistory.HistoryListBean();
                            PduOldLine pduOldLineTwo = new PduOldLine();
                            pduOldLineTwo = pduOldLineList.get(i);

                            historyListBean.setDatetime(pduOldLineTwo.getCollectiontime() + " 00:00:00");
                            historyListBean.setValue(Float.parseFloat(pduOldLineTwo.getResistance()));

                            powerList.add(historyListBean);
                        }
                        getResistanceHistory.setQuery_type(Integer.valueOf(query_type));
                        getResistanceHistory.setHistory_list(powerList);

                        MsgBean<GetResistanceHistory> msgBean = MsgBean.getInstance();
                        msgBean.setData(getResistanceHistory);

                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();

                    } else {

                        String errormsg = "该时间区域内，设备" + device_id + "没有记录！设备上线超过一个小时才有记录。";
                        MsgBean<GetResistanceHistory> msgBean = MsgBean.getFalseInstance(errormsg);
                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();
                    }
                } else {

                    PduOldLine pduOldLine = new PduOldLine();
                    pduOldLine.setPduID(ID);
                    pduOldLine.setStarttime(start_date);
                    pduOldLine.setEndtime(end_date);
                    List<PduOldLine> pduOldLineList = new ArrayList<PduOldLine>();

                    pduOldLineList = pduOldLineService.selectAllByHistoryHours(pduOldLine);

                    GetResistanceHistory getResistanceHistory = new GetResistanceHistory();
                    List<GetResistanceHistory.HistoryListBean> historyList = new ArrayList<GetResistanceHistory.HistoryListBean>();

                    if (pduOldLineList.size() > 0) {

                        for (int i = 0; i < pduOldLineList.size(); i++) {
                            GetResistanceHistory.HistoryListBean historyListBean = new GetResistanceHistory.HistoryListBean();
                            PduOldLine pduOldLineTwo = new PduOldLine();
                            pduOldLineTwo = pduOldLineList.get(i);

                            historyListBean.setDatetime(pduOldLineTwo.getCollectiontime());
                            historyListBean.setValue(Float.parseFloat(pduOldLineTwo.getResistance()));

                            historyList.add(historyListBean);
                        }
                        getResistanceHistory.setQuery_type(Integer.valueOf(query_type));
                        getResistanceHistory.setHistory_list(historyList);

                        MsgBean<GetResistanceHistory> msgBean = MsgBean.getInstance();
                        msgBean.setData(getResistanceHistory);

                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();

                    } else {

                        String errormsg = "该时间区域内，设备" + device_id + "没有记录！设备上线超过一个小时才有记录。";
                        MsgBean<GetPduInfoHistory> msgBean = MsgBean.getFalseInstance(errormsg);
                        PrintWriter out = response.getWriter();
                        out.print(msgBean.toJsonString());
                        out.flush();
                        out.close();
                    }

                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            PrintWriter out = response.getWriter();
            MsgBean msgBean = MsgBean.getTokenFalseInstance();
            out.print(msgBean.toJsonString());
            out.flush();
            out.close();
        }

    }


    public List<PduRelation> getTreeList(int topId, List<PduRelation> entityList) {
        List<PduRelation> resultList = new ArrayList();
        PduRelation pduSubChildrensBean = new PduRelation();
        try {
            //获取顶层元素集合
            int parentId;
            for (PduRelation entity : entityList) {
                parentId = Integer.valueOf(entity.getParent_id());
                if (topId == parentId) {

                    pduSubChildrensBean = getSubChildrensBean(entity.getPduID());

                    entity.setDevice_id(pduSubChildrensBean.getDevice_id());
                    entity.setDevice_name(pduSubChildrensBean.getDevice_name());
                    entity.setDevice_state(pduSubChildrensBean.getDevice_state());
                    entity.setGroup_id(pduSubChildrensBean.getGroup_id());
                    entity.setGroup_name(pduSubChildrensBean.getGroup_name());
                    entity.setPower(pduSubChildrensBean.getPower());
                    entity.setElectricity(pduSubChildrensBean.getElectricity());
                    entity.setVoltage(pduSubChildrensBean.getVoltage());
                    entity.setPdutype(pduSubChildrensBean.getPdutype());
                    entity.setParent_id(pduSubChildrensBean.getParent_id());
                    resultList.add(entity);

                }
            }

            //获取每个顶层元素的子数据集合
            for (PduRelation entity : resultList) {
                entity.setChildrens(getSubList(entity.getPduID(), entityList));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }

    /**
     * 获取子数据集合
     *
     * @param id
     * @param entityList
     * @return
     * @author jianda
     * @date 2017年5月29日
     */
    private List<PduRelation> getSubList(int id, List<PduRelation> entityList) {
        List<PduRelation> childList = new ArrayList<PduRelation>();
        String parentId;
        PduRelation pduSubChildrensBeantwo = new PduRelation();

        try {
            //子集的直接子对象
            for (PduRelation entity : entityList) {
                parentId = entity.getParent_id();
                if (String.valueOf(id).equals(parentId)) {

                    pduSubChildrensBeantwo = getSubChildrensBean(entity.getPduID());
                    System.out.println("设备ID==" + entity.getPduID());
                    entity.setDevice_id(pduSubChildrensBeantwo.getDevice_id());
                    entity.setDevice_name(pduSubChildrensBeantwo.getDevice_name());
                    entity.setDevice_state(pduSubChildrensBeantwo.getDevice_state());
                    entity.setGroup_id(pduSubChildrensBeantwo.getGroup_id());
                    entity.setGroup_name(pduSubChildrensBeantwo.getGroup_name());
                    entity.setPower(pduSubChildrensBeantwo.getPower());
                    entity.setElectricity(pduSubChildrensBeantwo.getElectricity());
                    entity.setVoltage(pduSubChildrensBeantwo.getVoltage());
                    entity.setPdutype(pduSubChildrensBeantwo.getPdutype());
                    entity.setParent_id(pduSubChildrensBeantwo.getParent_id());
                    childList.add(entity);

                }
            }

            //子集的间接子对象
            for (PduRelation entity : childList) {
                entity.setChildrens(getSubList(entity.getPduID(), entityList));
            }

            //递归退出条件
            if (childList.size() == 0) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return childList;
    }
}

