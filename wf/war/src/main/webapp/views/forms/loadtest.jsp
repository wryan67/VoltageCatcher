<%@ page import="com.acxiom.ccsi.idmgmt.constants.AppConstants" %>
<%@ page import="com.acxiom.bullseye.common.SettingsMBean" %>
<%@ page import="com.acxiom.ccsi.idmgmt.core.Util" %>
<%@ page import="org.apache.commons.lang.time.FastDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.acxiom.idmgmt.controllers.LoadtestController" %>

<%
    FastDateFormat userDate=FastDateFormat.getInstance("dd-MMM-yyyy hh:mm:ss a");

    SettingsMBean settings = (SettingsMBean) com.acxiom.txm.jmx.JMX.getMBean("com.acxiom.ccsi.idmgmt.mbeans:service=IDMSettings", SettingsMBean.class);

    String fromTime=(String)request.getSession().getAttribute("fromTime");
    String thruTime=(String)request.getSession().getAttribute("thruTime");

    if (Util.isBlankOrNull(fromTime)) {
        fromTime=userDate.format(new Date().getTime()-3600000);
    }
    if (Util.isBlankOrNull(thruTime)) {
        thruTime=userDate.format(new Date());
    }


%>
<style>
.boxcheck {
        margin-top: 4px;
        background-color: #ffffff;
        width: auto;
        height: 18px;
        padding-left: 0px;
        font-family: Arial;
        color: rgba(0, 0, 0, 0.5);
        font-size: 16px;
        font-weight: 400;
        text-align: left;
}

.bluetext {
	color: #000066;

	margin-top: 4px;
	margin-bottom: 0px;
    padding-left: 12px;

    font-family: Arial;
    font-size: 16px;
    font-weight: 400;
    text-align: left;
    line-height: 20px;

	background: lightgrey;
    border-color: #ADAAAD;
    border-style: solid;
    border-width: 1px;

    width: 410px;
    height: 22px;
}
.applyButton {
  width: 70px;
  top: 2px;
  vertical-align: middle;
  position: relative;
}
</style>

<div style="text-align:left; margin-top:15px;margin-bottom:15px;">
    <span style="text-wrap:none">
        <button name="buttonAction" onClick="location.href='/idmgmt/'" class="menuButton1" style="width: 150px;background-image: url('${param.baseURL}/assets/images/button1.jpg')" >
            Main Menu
        </button>
    </span>
</div>

<div class="genericForm" style='width:${param.formWidth}'>
    <h1><%=AppConstants.appTitle%></h1>
    <h2>Resource Access Control Center</h2>
    <h1><%=Util.getInstanceName().toUpperCase()%> Analysis</h1>
    <br/>

    <div style="text-align:left; margin-top:15px;margin-bottom:15px;">
    <span style="text-wrap:none">
        <button name="buttonAction" onClick="location.href='lt.create.jsp'" class="menuButton1" style="background-image: url('${param.baseURL}/assets/images/button1.jpg')" >
            Create Load Test
        </button>
        <br/>
        <button name="buttonAction" onClick="location.href='lt.run.jsp'" class="menuButton1" style="background-image: url('${param.baseURL}/assets/images/button1.jpg')" >
            Run Load Test
        </button>
        <br/>
        <button name="buttonAction" onClick="location.href='lt.monitor.jsp'" class="menuButton1" style="background-image: url('${param.baseURL}/assets/images/button1.jpg')" >
            Monitor Load Test
        </button>

        <br/>
        <%=LoadtestController.getMessage(request)%>

    </span>
    </div>

</div>


