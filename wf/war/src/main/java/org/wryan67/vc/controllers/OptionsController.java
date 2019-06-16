package org.wryan67.vc.controllers;

import org.apache.log4j.Logger;
import org.wryan67.vc.common.CookieJar;
import org.wryan67.vc.common.Util;
import org.wryan67.vc.database.tables.OPTIONS;
import org.wryan67.vc.database.util.Fetch;
import org.wryan67.vc.database.util.dbCaller;
import org.wryan67.vc.database.util.hibernate;
import org.wryan67.vc.models.OptionsModel;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wryan67.vc.controllers.SessionData.SessionVar.browserId;
import static org.wryan67.vc.controllers.SessionData.SessionVar.userOptions;

public class OptionsController {
    private static final Logger logger=Logger.getLogger(OptionsController.class);

    private static Fetch fetch = new Fetch(dbCaller.emf);

    public static OptionsModel getOptions(HttpServletRequest request, HttpServletResponse response) {

        if (SessionData.exists(request, userOptions)) {
            logger.info("got options from session");
            return SessionData.getValue(request, userOptions);
        }

        CookieJar cookieJar=new CookieJar(request,logger);


        if (cookieJar.containsKey(browserId.name())) {
            OPTIONS options=fetch.row("OPTIONS.getByUID","UID",cookieJar.getCookieValue(browserId.name()),logger);
            if (fetch!=null) {
                logger.info("got options from cookie");

                logger.info(options);
                logger.info("todo json to object");
                return new OptionsModel();
            }
        }


        return createNewOptions(request,response);

    }



    private static OptionsModel createNewOptions(HttpServletRequest request, HttpServletResponse response) {
        logger.info("creating new cookie");

        OptionsModel optionsModel=new OptionsModel();
        OPTIONS options=new OPTIONS();

        String id=Util.getGUID();
        options.setUID(id);
        options.setOPTIONS(optionsModel.toString());

        hibernate.insert(options,dbCaller.emf,logger);

        Cookie cookie = new Cookie(browserId.name(), id);
        cookie.setHttpOnly(false);
        cookie.setMaxAge(31536000);
        cookie.setPath("/");
        logger.info("cookie "+browserId.name()+" domain="+cookie.getDomain());
        response.addCookie(cookie);

        SessionData.setValue(request,userOptions,optionsModel);
        return optionsModel;

    }

}
