<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<%@include file="base.jsp" %>
<html lang="zh">
<head>
    <script type="text/javascript">
        function delPduGroup(id) {
            bootbox.confirm("<div style='line-height: 1.5;'>确定要删除吗?</div>", function (result){
                if(result){
                    window.location = '${path}/delPduGroup?id='+id;
                }
            });
        }
    </script>

    <title>设备分组</title>
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
                    <a href="/pdusearch">分组管理</a>
                    <i class="icon-angle-right"></i>
                </li>
                <li><a href="#">分组管理</a></li>
            </ul>

            <div class="row-fluid sortable">
                <div class="box span12">
                    <div class="box-header" data-original-title>
                        <h2><i class="halflings-icon white user"></i><span class="break"></span>设备分组表</h2>
                        <div class="box-icon">
                            <shiro:hasPermission name="sys:product:add">
                                <a  href="addPduGroup"><i class="halflings-icon white plus"></i></a>
                            </shiro:hasPermission>
                            <a href="#" class="btn-minimize"><i class="halflings-icon white chevron-up"></i></a>
                            <a href="#" class="btn-close"><i class="halflings-icon white remove"></i></a>
                        </div>
                    </div>
                    <div class="box-content">
                        <table class="table table-striped table-bordered bootstrap-datatable datatable">
                            <thead>
                            <tr>
                                <th>分组名称</th>
                                <th>分组说明</th>
                                <th>操作</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="PduGrouplist" items="${PduGrouplist}">
                                <tr>
                                    <td class="center">${PduGrouplist.groupname}</td>
                                    <td class="center">${PduGrouplist.remark}</td>

                                    <td class="center">
                                        <shiro:hasPermission name="sys:product:update">
                                            <a class="btn btn-info" href="${path}/seleceOnePduGroup?id=${PduGrouplist.id}">
                                                <i class="halflings-icon white edit" title='编辑'></i>
                                            </a>
                                        </shiro:hasPermission>
                                        <shiro:hasPermission name="sys:product:select">
                                            <a class="btn btn-success" href="${path}/PduGroupInfo?id=${PduGrouplist.id}">
                                                <i class="halflings-icon white zoom-in" title='分组详情'></i>
                                            </a>
                                        </shiro:hasPermission>
                                        <shiro:hasPermission name="sys:product:delete">
                                            <a class="btn btn-danger" onclick="delPduGroup(${PduGrouplist.id})">
                                                <i class="halflings-icon white trash" title='删除'></i>
                                            </a>
                                        </shiro:hasPermission>

                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
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
</body>
</html>
