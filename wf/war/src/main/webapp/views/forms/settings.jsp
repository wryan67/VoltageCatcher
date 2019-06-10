<%@ page import="org.wryan67.vc.common.AppConstants" %>
<%@ page import="org.wryan67.vc.common.jmx.JMX" %>
<%@ page import="org.wryan67.vc.mbeans.SettingsMBean" %>
<%@ page import="org.wryan67.vc.controllers.SessionData" %>
<%@ page import="static org.wryan67.vc.controllers.SessionData.SessionVar.userMessage" %>

<%
    SettingsMBean settings = (SettingsMBean) JMX.getMBean("org.wryan67.vc.mbeans:service=Settings", SettingsMBean.class);

    String userMsg = SessionData.getValueAndRemove(request, userMessage);


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
    <h2>Settings</h2>
    <!-- came from <%=request.getServerName()+":"+request.getServerPort()%>  -->
    <br/>

	<form method=post>
	    <input type=hidden name=action value="update">
		<table>

            <table>
                <tr>
                    <td>Debug Mode</td>
                    <td><input name="debug" type="checkbox" value="true" <%=(settings.getDebug())?"checked":""%>></td>
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
                    <%=(userMsg ==null)?"":"<br>"+ userMsg%>
                </div>
              </td>
            </tr>
		</table>
    </form>
</div>


