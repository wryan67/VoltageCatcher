package org.wryan67.vc.controllers;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MonitorController {
    private static final Logger logger=Logger.getLogger(MonitorController.class);


    public static boolean process(HttpServletRequest request, HttpServletResponse response) {

        logger.info("monitor method1");

        return false;
    }
}
