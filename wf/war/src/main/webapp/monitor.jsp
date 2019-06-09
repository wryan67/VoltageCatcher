<%@include file="views/shared/env.jsp"%>
<jsp:include page="views/shared/layout.jsp">
    <jsp:param name="baseURL"   value="<%=baseURL%>" />
    <jsp:param name="modelForm" value="../forms/monitor.jsp"/>
    <jsp:param name="formWidth" value="300px" />
</jsp:include>
