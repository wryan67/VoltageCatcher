<%@ page import="com.acxiom.ccsi.idmgmt.constants.AppConstants" %>
<%@ page import="com.acxiom.ccsi.idmgmt.mbeans.IDMSettingsMBean" %>

<%
    IDMSettingsMBean settings = (IDMSettingsMBean) com.acxiom.txm.jmx.JMX.getMBean("com.acxiom.ccsi.idmgmt.mbeans:service=IDMSettings", IDMSettingsMBean.class);

    String userMessage=(String)request.getSession().getAttribute("userMessage");
    request.getSession().removeAttribute("userMessage");

    String mc1sel="";
    String mc2sel="";

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

<div class="genericForm" style='width:${param.formWidth}'>
    <h1><%=AppConstants.appTitle%></h1>
    <h2>Resource Access Control Center</h2>
    <h1>Settings</h1>
    <!-- came from <%=request.getServerName()+":"+request.getServerPort()%>  -->
    <br/>

	<form method=post>
	    <input type=hidden name=action value="update">
		<table>

            <table>
                <tr>
                    <td>Debug Mode</td>
                    <td><input name="debug" type="checkbox" value="true" <%=(settings.isDebug())?"checked":""%>></td>
                </tr>
                <tr>
                    <td>Log Headers</td>
                    <td><input name="logHeaders" type="checkbox" value="true" <%=(settings.isLogHeaders())?"checked":""%>></td>
                </tr>
                <tr>
                    <td>Log IO</td>
                    <td><input name="logIO" type="checkbox" value="true" <%=(settings.isLogIO())?"checked":""%>></td>
                </tr>
                <tr>
                    <td>Log CommonTiming</td>
                    <td><input name="logCommonTiming" type="checkbox" value="true" <%=(settings.isLogCommonTiming())?"checked":""%>></td>
                </tr>

                <tr>
                    <td>Shrink Output</td>
                    <td><input name="shrink" type="checkbox" value="true" <%=(settings.isShrinkOutput())?"checked":""%>></td>
                </tr>




            </table>
            <tr>
              <td></td>
              <td></td>
              <td>
                <div style="text-align:center; margin-top:15px;margin-bottom:15px;">
                    <button name="buttonAction" onClick="location.href='settings.jsp'" class="button1" style="background-image: url('${param.baseURL}/assets/images/button1.jpg')" >
                         Apply
                    </button>
                    <%=(userMessage==null)?"":"<br>"+userMessage%>
                </div>
              </td>
            </tr>
		</table>
    </form>
</div>


