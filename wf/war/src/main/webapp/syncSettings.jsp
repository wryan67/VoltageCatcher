<%@ page import="com.acxiom.ccsi.idmgmt.controllers.SyncSettingsController" %>
<%@include file="views/shared/env.jsp"%>
<%

    if (SyncSettingsController.process(request, response)) {
        return;
    }

%>

<jsp:include page="views/shared/layout.jsp">
    <jsp:param name="baseURL"                value="<%=baseURL%>" />
    <jsp:param name="modelForm"              value="../forms/syncSettings.jsp"/>
    <jsp:param name="formWidth"              value="610px" />
    <jsp:param name="onPageLoad"             value="loadCookies()" />
    <jsp:param name="contentWrapperHeight"   value="min-height:600px" />
</jsp:include>