<%@ page import="org.wryan67.vc.controllers.MonitorController" %>
<%@ page import="org.wryan67.vc.controllers.OptionsController" %>
<%@ page import="org.wryan67.vc.models.OptionsModel" %>
<%@include file="views/shared/env.jsp"%><%
    OptionsModel options = OptionsController.getOptions(request,response);

    if (MonitorController.process(request, response)) {
        throw new javax.servlet.jsp.SkipPageException();
    }

%>
<jsp:include page="views/shared/layout.jsp">
    <jsp:param name="baseURL"   value="<%=baseURL%>" />
    <jsp:param name="options"   value="<%=options%>" />
    <jsp:param name="modelForm" value="../forms/monitor.jsp"/>
    <jsp:param name="formWidth" value="1170px" />

</jsp:include>
