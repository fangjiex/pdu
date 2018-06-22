<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<%@include file="base.jsp" %>
<html lang="zh">
<head>

    <title>自检管理</title>
</head>
<body>
<!-- start: Header -->
<%--顶部--%>
<%@include file="head.jsp" %>
<%--顶部--%>
<div class="container-fluid-full">
    <div class="row-fluid">
        <jsp:include page="menu.jsp"></jsp:include>
        <div id="content" class="span10">

            <ul class="breadcrumb">
                <li>
                    <i class="icon-home"></i>
                    <a href="/pduSelfchecklist">自检管理</a>
                    <i class="icon-angle-right"></i>
                </li>
                <li>
                    <i class="icon-plus"></i>
                    <a href="#">新增</a>
                </li>
            </ul>


            <div class="row-fluid sortable">
                <form action="">
                    <div class="box span12">
                        <div class="box-header" data-original-title>
                            <h2><i class="halflings-icon white edit"></i><span class="break"></span>自检设置2</h2>
                            <div class="box-icon">
                                <!--
                                    <a href="#" class="btn-setting"><i class="halflings-icon white wrench"></i></a>
                               -->
                                <a href="#" class="btn-minimize"><i class="halflings-icon white chevron-up"></i></a>
                                <a href="#" class="btn-close"><i class="halflings-icon white remove"></i></a>
                            </div>
                        </div>
                        <div class="box-content">

                            <table class="table table-striped table-bordered bootstrap-datatable datatable">
                                <thead>
                                <tr>
                                    <th>设备选择</th>
                                    <th>设备名称</th>
                                    <th>设备IP</th>
                                    <th>设备编号</th>
                                    <th>在线状态</th>

                                </tr>
                                </thead>
                                <tbody>

                                <!--获取后台的数据列表，循环输出-->
                                <c:choose>
                                    <c:when test="${not empty pduList}">
                                        <c:forEach items="${pduList}" var="pdu" varStatus="vs">
                                            <tr>
                                                <td><input type="checkbox" value="${pdu.id}"name="pdu"></td>
                                                <td>${pdu.name}</td>
                                                <td class="center">${pdu.ip}</td>
                                                <td class="center">${pdu.qrcode}</td>
                                                <td class="center">
                                                    <c:if test="${pdu.state == '1' }"><span
                                                            class="label label-success">在线</span></c:if>
                                                    <c:if test="${pdu.state == '0' }"><span
                                                            class="label label-important">离线</span></c:if>
                                                </td>

                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                </c:choose>

                                </tbody>
                            </table>
                            <div id="tijiao" style="margin-left:43%;"margin:auto;>
                                <button type="submit" class="btn btn-primary" onclick="addUser()"> 提交 </button>
                                <button class="btn" onclick="javascript:history.back(-1);"> 返回 </button>
                            </div>
                        </div>
                    </div><!--/span-->
                </form>
            </div>
        </div><!--/span-->

    </div><!--/row-->

</div>
</div>
</div>
<div class="modal hide fade" id="myModal">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">×</button>
        <h3>Settings</h3>
    </div>
    <div class="modal-body">
        <p>Here settings can be configured...</p>
    </div>
    <div class="modal-footer">
        <a href="#" class="btn" data-dismiss="modal">Close</a>
        <a href="#" class="btn btn-primary">Save changes</a>
    </div>
</div>

<div class="common-modal modal fade" id="common-Modal1" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-content">
        <ul class="list-inline item-details">
            <li><a href="#">Admin templates</a></li>
            <li><a href="http://themescloud.org">Bootstrap themes</a></li>
        </ul>
    </div>
</div>
<%@include file="foot.jsp"%>
<script type="text/javascript">
    function addUser() {
        var form = document.forms[0];
        form.action = "<%=basePath %>updateUser";
        form.method = "post";
        form.submit();
    }
</script>
</body>
</html>
