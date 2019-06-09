package org.wryan67.vc.controllers;

import org.apache.log4j.Logger;
import org.wryan67.vc.common.Util;
import org.wryan67.vc.models.OptionsModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.wryan67.vc.controllers.SessionData.SessionVar.userMessage;
import static org.wryan67.vc.models.OptionsModel.OptionFields.*;
import static org.wryan67.vc.models.OptionsModel.*;


public class MonitorController {


    private static final Logger logger=Logger.getLogger(MonitorController.class);


    public static boolean process(HttpServletRequest request, HttpServletResponse response) {
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

        return false;
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
                if (options.triggerVoltage<minTriggerVoltage || options.triggerVoltage>maxTriggerVoltage) {
                    messages.add("trigger voltage out of valid range");
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

        if (exists(request, headers)) {
            options.headers=getParameter(request,headers).equals("true")?true:false;
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

            SessionData.setValue(request,userMessage,"<br>"+Util.join(messages,"<br>"));

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
