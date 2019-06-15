package org.wryan67.vc.org.wryan67.vc.war;

import org.apache.log4j.Logger;
import org.wryan67.vc.controllers.MonitorController;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(value = {"/stop"})

public class StopMonitor extends HttpServlet {
    private static final Logger logger=Logger.getLogger(MonitorController.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        VCReader.stopMonitor();
    }
}
