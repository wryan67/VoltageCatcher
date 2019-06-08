<%@ page import="org.wryan67.vc.common.AppConstants" %>
<div class="genericForm" style='width:${param.formWidth}'>
    <h1><%=AppConstants.appTitle%></h1>
    <h2>Main Menu</h2>

    <div style="text-align:left; margin-top:15px;margin-bottom:15px;">
    <span style="text-wrap:none">
        <button name="buttonAction" onClick="location.href='monitor.jsp'" class="menuButton1" style="background-image: url('${param.baseURL}/assets/images/button1.jpg')" >
            Monitor
        </button>
        <button name="buttonAction" onClick="location.href='settings.jsp'" class="menuButton1" style="background-image: url('${param.baseURL}/assets/images/button1.jpg')" >
            Settings
        </button>
    </span>
    </div>
</div>
