<%@ page import="org.wryan67.vc.common.AppConstants" %>
<%@ page import="org.wryan67.vc.common.Util" %>
<%@ page import="org.wryan67.vc.common.jmx.JMX" %>

<%@ page import="org.wryan67.vc.controllers.SessionData" %>
<%@ page import="static org.wryan67.vc.models.OptionsModel.OptionFields.frequency" %>
<%@ page import="static org.wryan67.vc.controllers.SessionData.SessionVar.userMessage" %>
<%@ page import="org.wryan67.vc.mbeans.SettingsMBean" %>
<%@ page import="static org.wryan67.vc.models.OptionsModel.OptionFields.triggerVoltage" %>
<%@ page import="static org.wryan67.vc.models.OptionsModel.OptionFields.*" %>
<%@ page import="static org.wryan67.vc.controllers.SessionData.SessionVar.userOptions" %>
<%@ page import="org.wryan67.vc.models.OptionsModel" %>
<%@ page import="org.wryan67.vc.models.VCOutputFormat" %>

<%
    SettingsMBean settings = (SettingsMBean) JMX.getMBean("org.wryan67.vc.mbeans:service=Settings", SettingsMBean.class);

    String userMsg = SessionData.getValueAndRemove(request, userMessage);

    OptionsModel options = SessionData.getValueOrDefault(request, userOptions, new OptionsModel());


    String mc1sel="";
    String mc2sel="";

    boolean debug = settings.getDebug();

%>

<script type="text/javascript" src="${param.baseURL}/assets/js/smoothie.js"></script>


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
    <h2>Monitor/Catcher</h2>

    <canvas id="mycanvas" width="1000" height="100"></canvas>

    <form method=post>
        <input type=hidden name=action value="capture">
        <table>

            <table>
                <tr>
                    <td>Frequency (5-75) kHz</td>
                    <td><input name="<%=frequency%>" type="text" value="<%=options.frequency%>" ></td>
                    <td>&nbsp;</td>

                    <td>Trigger voltage</td>
                    <td><input name="<%=triggerVoltage%>" type="text" value="<%=options.triggerVoltage%>" ></td>
                    <td>&nbsp;</td>
                </tr>
                <tr>
                    <td>Samples (1-40000)</td>
                    <td><input name="<%=samples%>" type="text" value="<%=options.samples%>" ></td>
                    <td>&nbsp;</td>

                    <td>Channels (0-7) csv</td>
                    <td><input name="<%=channels%>" type="text" value="<%=Util.join(options.channels,",")%>" ></td>
                    <td>&nbsp;</td>
                </tr>
                <tr>
                    <td>Headers</td>
                    <td><input name="<%=headers%>" type="checkbox" value="true" <%=(options.headers)?"checked":""%>></td>
                    <td>&nbsp;</td>

                    <td>Output filename</td>
                    <td><input name="<%=outputFilename%>" type="text" value="<%=options.outputFilename%>" ></td>
                    <td>&nbsp;</td>
                </tr>
                <tr>
                    <td>verbose</td>
                    <td><input name="<%=verbose%>" type="checkbox" value="true" <%=(options.verbose)?"checked":""%>></td>
                    <td>&nbsp;</td>

                    <td>Output format</td>
                    <td>
                        <% for (VCOutputFormat value : VCOutputFormat.values()) { %>
                            <input type="radio" name="<%=outputFormat%>" value="<%=value%>" <%=(options.outputFormat==value)?"checked":""%>>
                                <span style="position:relative; top:-5px;">
                                    <%=value%>
                                </span>
                            </input> &nbsp;&nbsp;&nbsp;&nbsp;
                        <% } %>
                    </td>

                    <td>&nbsp;</td>
                </tr>
            </table>
            <tr>
                <td colspan="5">
                    <div style="text-align:center; margin-top:15px;margin-bottom:15px;">
                        <button name="buttonAction" onClick="location.href='settings.jsp'" class="button1" style="background-image: url('${param.baseURL}/assets/images/button1.jpg')" >
                            Capture
                        </button>
                        <% if (userMsg!=null) { %>
                          <br><%=userMsg%>
                          <img src="chart.jpg">
                        <%}%>
                    </div>
                </td>
            </tr>
        </table>
    </form>
</div>

<!--
<script>
    var smoothie = new SmoothieChart();
    smoothie.streamTo(document.getElementById("mycanvas"));

    // Data
    var line1 = new TimeSeries();
    var line2 = new TimeSeries();

    // Add a random value to each line every second
    setInterval(function() {
        line1.append(new Date().getTime(), Math.random());
        line2.append(new Date().getTime(), Math.random());
    }, 500);

    // Add to SmoothieChart
    smoothie.addTimeSeries(line1);
    smoothie.addTimeSeries(line2);

    smoothie.streamTo(document.getElementById("mycanvas"), 1000 /*delay*/);

</script>
-->

<%  if (SessionData.exists(request, SessionData.SessionVar.file2download)) { %>

    <script>
        // var xhr = new XMLHttpRequest();
        // xhr.open("GET", "download");
        // xhr.send();

        window.location="download";
    </script>


<% } %>