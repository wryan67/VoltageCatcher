<%@ page import="org.wryan67.vc.controllers.MonitorController" %>
<%@include file="views/shared/env.jsp"%>
<%

    if (MonitorController.process(request, response)) {
        return;
    }

%>
<jsp:include page="views/shared/layout.jsp">
    <jsp:param name="baseURL"   value="<%=baseURL%>" />
    <jsp:param name="modelForm" value="../forms/monitor.jsp"/>
    <jsp:param name="formWidth" value="1170px" />
</jsp:include>
