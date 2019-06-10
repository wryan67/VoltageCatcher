package org.wryan67.vc.org.wryan67.vc.war;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.wryan67.vc.controllers.MonitorController;
import org.wryan67.vc.controllers.SessionData;
import org.wryan67.vc.models.OptionsModel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@WebServlet(value = {"/download"})

public class DownloadFile extends HttpServlet {
    private static final Logger logger=Logger.getLogger(MonitorController.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        OptionsModel options= SessionData.getValueOrDefault(request,SessionData.SessionVar.userOptions,new OptionsModel());

        try {
            SessionData.remove(request, SessionData.SessionVar.file2download);

//            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", options.outputFilename));

            FileUtils.copyFile(new File("/tmp/data.csv"), response.getOutputStream());

        } catch (IOException e) {
            logger.error("cannot read/write /tmp/data.csv",e);
        }
    }
}
