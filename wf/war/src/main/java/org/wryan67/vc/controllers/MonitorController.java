package org.wryan67.vc.controllers;

import org.apache.log4j.Logger;
import org.wryan67.vc.common.CookieJar;
import org.wryan67.vc.common.Util;
import org.wryan67.vc.database.tables.OPTIONS;
import org.wryan67.vc.database.util.dbCaller;
import org.wryan67.vc.database.util.hibernate;
import org.wryan67.vc.models.OptionsModel;
import org.wryan67.vc.models.SupportedChartTypes;
import org.wryan67.vc.org.wryan67.vc.war.VCReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.SkipPageException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.wryan67.vc.controllers.SessionData.SessionVar.*;
import static org.wryan67.vc.models.OptionsModel.OptionFields.*;
import static org.wryan67.vc.models.OptionsModel.*;


public class MonitorController {
    private static final Logger logger=Logger.getLogger(MonitorController.class);
    private static final String thisPage="monitor.jsp";


    public static boolean process(HttpServletRequest request, HttpServletResponse response) throws SkipPageException {
        String action=request.getParameter("action");

        MonitorController.killvc();


        logger.info("monitor action="+action);
        OptionsModel options=SessionData.getValueOrDefault(request,SessionData.SessionVar.userOptions,new OptionsModel());

        if (action==null) {
            VCReader.startMonitor(options);
            return false;
        }

        
        switch (action) {
            case "capture": {
                return capture(request, response, options);
            }

            default:
                SessionData.setValue(request, userMessage, "Unknown method called");
                return false;
        }

    }



    private static boolean capture(HttpServletRequest request, HttpServletResponse response, OptionsModel options) {
        VCReader.stopMonitor();

        try {
            Thread.sleep(100);
        } catch (Exception e) {}

        SessionData.setValue(request, status, "failed");

        if (!validateInput(request,options)) {
            return false;
        }

        CookieJar cookieJar = new CookieJar(request,logger);
        if (cookieJar.containsKey(browserId.name())) {
            OPTIONS row = new OPTIONS();
            row.setUID(cookieJar.getCookieValue(browserId.name()));
            row.setOPTIONS(options.toJson(true));
            hibernate.updateHibernateObject(row, dbCaller.emf,logger);
        }

        logger.info("capturing data");
        logger.info(options.toString());



        String cmd=buildCommand(options, "/tmp/data.csv");
        logger.info(cmd);
        List<String> messages=new ArrayList<>();

        try {

            Process p = Runtime.getRuntime().exec(cmd);

            try {
                p.waitFor();
            } catch (InterruptedException e) {}


            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));


            String s;
            while ((s = stdInput.readLine()) != null) {
                messages.add(s.replaceAll("elapsed.*s=","elapsed &micro;s="));
            }

            while ((s = stdError.readLine()) != null) {
                messages.add(s);
            }


            SessionData.setValue(request, SessionData.SessionVar.file2download,"/tmp/data.csv");
            blockResponse(request,messages);

            if (p.exitValue()==0) {
                SessionData.setValue(request, status, "success");
            }

            VCReader.startMonitor(options);
            return false;
        } catch (IOException e) {
            logger.error("system command failed",e);
            messages.add("capture failed: "+e.getMessage());
            blockResponse(request,messages);

            return false;
        }
    }



    public static String buildCommand(OptionsModel options, String interfaceFile) {
        String sudo=(Util.whoami().equals("root"))?"":"sudo";

        return String.format("%s ionice -c1 -n0 nice -n -20 /usr/local/bin/vc %s %s -c %s -t %f -f %d -s %d -o %s",
                sudo,
                (options.verbose)?"-v":"",
                (options.headers)?"":"-h",
                Util.join(options.channels,","),
                options.triggerVoltage, options.frequency, options.samples, interfaceFile
        );
    }

    private static void blockResponse(HttpServletRequest request, List<String> messages) {
        StringBuilder rs=new StringBuilder(1024);

        rs.append("<br/><div style='text-align:left'>\n");
        rs.append("<pre>");

        rs.append(Util.join(messages,"\n"));

        rs.append("</pre>");
        rs.append("</div>");

        SessionData.setValue(request, userMessage, rs.toString());
    }

    private static boolean validateInput(HttpServletRequest request, OptionsModel options) {
        List<String> messages=new ArrayList<>();

        if (exists(request, samples)) {
            try {
                options.samples=new Integer(getParameter(request,samples));
                if (options.samples<minSamples || options.samples>maxSamples) {
                    messages.add(String.format("samples out of valid range: [%d-%d]",minSamples,maxSamples));
                }
            } catch (Exception e) {
                messages.add("samples contains invalid characters: "+getParameter(request,samples));
            }
        }

        if (exists(request, frequency)) {
            try {
                options.frequency=new Integer(getParameter(request,frequency));
                if (options.frequency<minFrequency || options.frequency>maxFrequency) {
                    messages.add("frequency out of valid range");
                }
            } catch (Exception e) {
                messages.add("frequency contains invalid characters: "+getParameter(request,frequency));
            }
        }

        if (exists(request, triggerVoltage)) {
            try {
                options.triggerVoltage=new Float(getParameter(request,triggerVoltage));
                if (options.triggerVoltage!=0.0) {
                    if (Math.abs(options.triggerVoltage) < minTriggerVoltage || Math.abs(options.triggerVoltage) > maxTriggerVoltage) {
                        messages.add("trigger voltage out of valid range");
                    }
                }
            } catch (Exception e) {
                messages.add("trigger voltage contained invalid characters: "+getParameter(request,triggerVoltage));
            }
        }

        
        if (exists(request, channels)) {
            try {
                options.channels=new ArrayList<>();
                for (String c : getParameter(request, channels).split(",")) {
                    int channel=new Integer(c);
                    if (channel<minChannel || channel>maxChannel) {
                        messages.add("channel ["+c+"] out of valid range");
                    }
                    options.channels.add(channel);
                }
                if (options.channels.size()<minChannels || options.channels.size()>maxChannels) {
                    messages.add("invalid number of channels, range is ["+minChannels+"-"+maxChannels+"]");
                }
            } catch (Exception e) {
                messages.add("channels contains invalid characters: "+getParameter(request,channels));
            }
        }

        if (exists(request, verbose)) {
            options.verbose=getParameter(request,verbose).equals("true")?true:false;
        } else {
            options.verbose=false;
        }


        if (exists(request, headers)) {
            options.headers=getParameter(request,headers).equals("true")?true:false;
        } else {
            options.headers=false;
        }


        if (exists(request, chartType)) {
            options.chartType = SupportedChartTypes.valueOf(getParameter(request,chartType));
        }

        if (exists(request, outputFilename)) {
            String fileName=getParameter(request,outputFilename);
            if (!Util.isBlankOrNull(fileName)) {
                options.outputFilename=fileName;
                if (!fileName.matches("[-_.A-Za-z0-9]*")) {
                    messages.add("output file name contains invalid characters");
                }
            } else {
                messages.add("output file name may not be blank");
            }
        }

        if (messages.size()<1) {
            return true;
        } else {
            blockResponse(request, messages);
            return false;
        }

    }
    
    
    private static String getParameter(HttpServletRequest request, OptionsModel.OptionFields parm) {
        return request.getParameter(parm.toString());
    }

    private static boolean exists(HttpServletRequest request, OptionsModel.OptionFields parm) {
        if (request.getParameter(parm.toString())==null) {
            return false;
        } else {
            return true;
        }
    }

    public static void killvc() {


        try {
            Process p = Runtime.getRuntime().exec("/usr/local/bin/killvc");
            p.waitFor();
        } catch (IOException e) {

        } catch (InterruptedException e) {

        }

    }
}
