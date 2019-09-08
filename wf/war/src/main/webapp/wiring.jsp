<%@ page import="org.wryan67.vc.controllers.SettingsController" %>
<%@include file="views/shared/env.jsp"%>
<%

    if (SettingsController.process(request, response)) {
        return;
    }

%>
<jsp:include page="views/shared/layout.jsp">
    <jsp:param name="baseURL"                value="<%=baseURL%>" />
    <jsp:param name="modelForm"              value="../forms/wiring.jsp"/>
    <jsp:param name="formWidth"              value="1000px" />
    <jsp:param name="onPageLoad"             value="loadCookies()" />
    <jsp:param name="contentWrapperHeight"   value="min-height:600px" />
</jsp:include>