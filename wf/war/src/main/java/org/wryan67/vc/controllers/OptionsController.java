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
import java.io.IOException;

import static org.wryan67.vc.controllers.SessionData.SessionVar.browserId;
import static org.wryan67.vc.controllers.SessionData.SessionVar.userOptions;

public class OptionsController {
    private static final Logger logger=Logger.getLogger(OptionsController.class);

    private static Fetch fetch = new Fetch(dbCaller.emf);

    public static OptionsModel getOptions(HttpServletRequest request, HttpServletResponse response) {

        CookieJar cookieJar=new CookieJar(request,logger);


        if (SessionData.exists(request, userOptions)) {
            OptionsModel optionsModel=SessionData.getValue(request, userOptions);
            logger.info("got options from session");

            return optionsModel;
        }



        if (cookieJar.containsKey(browserId.name())) {
            OPTIONS options=fetch.row("OPTIONS.getByUID","UID",cookieJar.getCookieValue(browserId.name()),logger);
            if (options!=null) {
                logger.info("got options from cookie");

                try {
                    return new OptionsModel().fromJson(options.getOPTIONS());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.info("cookie jar is empty");
        }

        return setCookie(request,response,new OptionsModel());
    }



    private static OptionsModel setCookie(HttpServletRequest request, HttpServletResponse response, OptionsModel optionsModel) {
        logger.info("creating new cookie");

        OPTIONS options=new OPTIONS();

        String id=Util.getGUID();
        options.setUID(id);
        options.setOPTIONS(optionsModel.toJson(true));

        hibernate.insert(options,dbCaller.emf,logger);
        SessionData.setValue(request,userOptions,optionsModel);

        Cookie cookie = new Cookie(browserId.name(), id);
        cookie.setHttpOnly(false);
        cookie.setMaxAge(31536000);
        cookie.setPath("/");
        logger.info("cookie "+browserId.name()+" domain="+cookie.getDomain());
        response.addCookie(cookie);

        return optionsModel;

    }

}
