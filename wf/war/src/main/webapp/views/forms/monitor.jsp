<%@ page import="org.wryan67.vc.common.AppConstants" %>
<%@ page import="org.wryan67.vc.common.Util" %>
<%@ page import="org.wryan67.vc.common.jmx.JMX" %>
<%@ page import="org.wryan67.vc.controllers.SessionData" %>
<%@ page import="static org.wryan67.vc.models.OptionsModel.OptionFields.*" %>
<%@ page import="org.wryan67.vc.mbeans.SettingsMBean" %>
<%@ page import="static org.wryan67.vc.controllers.SessionData.SessionVar.*" %>
<%@ page import="static org.wryan67.vc.controllers.SessionData.SessionVar.userOptions" %>
<%@ page import="org.wryan67.vc.models.OptionsModel" %>
<%@ page import="org.wryan67.vc.models.VCOutputFormat" %>
<%@ page import="static java.lang.Boolean.FALSE" %>
<%@ page import="org.wryan67.vc.models.SupportedChartTypes" %>
<%@ page import="org.wryan67.vc.controllers.MonitorController" %>
<%@ page import="org.wryan67.vc.controllers.OptionsController" %>
<%
    SettingsMBean settings = (SettingsMBean) JMX.getMBean("org.wryan67.vc.mbeans:service=Settings", SettingsMBean.class);

    String userMsg = SessionData.getValueAndRemove(request, userMessage);
    OptionsModel options = OptionsController.getOptions(request,response);


    int imageTimeout=150+(options.channels.size()*150);



    String mc1sel="";
    String mc2sel="";

    boolean debug = settings.getDebug();
    boolean success = SessionData.getValueOrDefault(request,status,"failed").equals("success");

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

<!--
    <canvas id="mycanvas" width="1000" height="100"></canvas>
-->
    <img id="rtImage" src="x" width="1125" height="300">

    <form method=post>
        <input type=hidden name=action value="capture">
        <table>
            <tr>
                <td>Frequency (5-75) kHz</td>
                <td><input name="<%=frequency%>" type="text" value="<%=options.frequency%>" ></td>
                <td>&nbsp;</td>

                <td>Trigger voltage</td>
                <td><input name="<%=triggerVoltage%>" type="text" value="<%=options.triggerVoltage%>" ></td>
            </tr>
            <tr>
                <td>Samples (1-40000)</td>
                <td><input name="<%=samples%>" type="text" value="<%=options.samples%>" ></td>
                <td>&nbsp;</td>

                <td>Channels (0-7) csv</td>
                <td><input name="<%=channels%>" type="text" value="<%=Util.join(options.channels,",")%>" ></td>
            </tr>
            <tr>
                <td>Headers</td>
                <td><input name="<%=headers%>" type="checkbox" value="true" <%=(options.headers)?"checked":""%>></td>
                <td>&nbsp;</td>

                <td>Output filename</td>
                <td><input name="<%=outputFilename%>" type="text" value="<%=options.outputFilename%>" ></td>
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

            </tr>
            <tr>
                <td>Chart type</td>
                <td>
                    <% for (SupportedChartTypes value : SupportedChartTypes.values()) { %>
                    <input type="radio" name="<%=chartType%>" value="<%=value%>" <%=(options.chartType==value)?"checked":""%>>
                    <span style="position:relative; top:-5px;">
                                <%=value%>
                            </span>
                    </input> &nbsp;&nbsp;&nbsp;&nbsp;
                    <% } %>
                </td>

                <td>&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2">
                    <div style="text-align:left; margin-top:15px;margin-bottom:15px;">
                        <button name="buttonAction" onClick="location.href='monitor.jsp'" class="button1" style="background-image: url('${param.baseURL}/assets/images/button1.jpg')" >
                            Capture
                        </button>
                    </div>
                </td>
                <td style="text-align:left">
                    <%  if (SessionData.exists(request, SessionData.SessionVar.file2download)) { %>
                    <div style="text-align:center; margin-top:15px;margin-bottom:15px;background-image: url('${param.baseURL}/assets/images/button1.jpg')"
                         class="button1" onClick="window.open('download')">
                            Download
                    </div>
                    <% } %>
                </td>
                <td colspan=2 style="text-align:right">
                    <div style="text-align:center; margin-top:15px;margin-bottom:15px;background-image: url('${param.baseURL}/assets/images/button1.jpg')"
                         class="button1" onClick="xhrStop()">
                        Stop
                    </div>

                </td>
            </tr>
            <tr>
                <td colspan="5">
                    <div>
                        <%=(userMsg==null)?"":"<br>"+userMsg%>

                        <% if (success && SessionData.exists(request, SessionData.SessionVar.file2download)) { %>
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

<script>

    function rtImageUpdate() {
        var imgReplace = document.getElementById("rtImage");
            imgReplace.style.visibility = "false";
            imgReplace.src = "rtchart.jpg?ts="+Date.now();
            imgReplace.style.visibility = "visible";
    }
    function rtImageUpdate2() {
        rtImageUpdate()
        setTimeout(rtImageUpdate2, <%=imageTimeout%>);
    }

    function rtImageUpdate1() {
        rtImageUpdate()
        setTimeout(rtImageUpdate2, 2000);
    }

    setTimeout(rtImageUpdate1, 1000);
</script>


<script>
    function xhrStop() {
        var oReq = new XMLHttpRequest();
        oReq.open("GET", "stop");
        oReq.send();
    }
</script>