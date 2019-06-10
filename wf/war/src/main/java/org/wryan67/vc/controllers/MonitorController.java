package org.wryan67.vc.controllers;

import org.apache.log4j.Logger;
import org.wryan67.vc.common.Util;
import org.wryan67.vc.models.OptionsModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.SkipPageException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.wryan67.vc.controllers.SessionData.SessionVar.userMessage;
import static org.wryan67.vc.models.OptionsModel.OptionFields.*;
import static org.wryan67.vc.models.OptionsModel.*;


public class MonitorController {


    private static final Logger logger=Logger.getLogger(MonitorController.class);
    private static final String thisPage="monitor.jsp";


    public static boolean process(HttpServletRequest request, HttpServletResponse response) throws SkipPageException {
        String action=request.getParameter("action");

        logger.info("monitor action="+action);

        if (action==null) {
            return false;
        }

        
        switch (action) {
            case "capture": {
                return capture(request, response);
            }

            default:
                SessionData.setValue(request, userMessage, "Unknown method called");
                return false;
        }

    }


    private static boolean capture(HttpServletRequest request, HttpServletResponse response) {
        OptionsModel options=SessionData.getValueOrDefault(request,SessionData.SessionVar.userOptions,new OptionsModel());


        if (!validInput(request,options)) {
            return false;
        }

        logger.info("capturing data");
        logger.info(options.toString());

        String cmd=String.format("sudo /usr/local/bin/vc %s %s -c %s -t %f -f %d -s %d -o /tmp/data.csv",
                (options.verbose)?"-v":"",
                (options.headers)?"":"-h",
                Util.join(options.channels,","),
                options.triggerVoltage, options.frequency, options.samples
        );

        logger.info(cmd);
        List<String> messages=new ArrayList<>();

        try {

            Process p = Runtime.getRuntime().exec(cmd);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String s;
            while ((s = stdInput.readLine()) != null) {
                messages.add(s);
            }

            while ((s = stdError.readLine()) != null) {
                messages.add(s);
            }

            SessionData.setValue(request, SessionData.SessionVar.file2download,"/tmp/data.csv");
            blockResponse(request,messages);
            return false;
        } catch (IOException e) {
            logger.error("system command failed",e);
            messages.add("capture failed: "+e.getMessage());
            blockResponse(request,messages);

            return false;
        }





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

    private static boolean validInput(HttpServletRequest request, OptionsModel options) {
        List<String> messages=new ArrayList<>();

        if (exists(request, samples)) {
            try {
                options.samples=new Integer(getParameter(request,samples));
                if (options.samples<minSamples || options.samples>maxSamples) {
                    messages.add("samples out of valid range");
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
                options.triggerVoltage=new Double(getParameter(request,triggerVoltage));
                if (options.triggerVoltage!=0.0) {
                    if (options.triggerVoltage < minTriggerVoltage || options.triggerVoltage > maxTriggerVoltage) {
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
}
