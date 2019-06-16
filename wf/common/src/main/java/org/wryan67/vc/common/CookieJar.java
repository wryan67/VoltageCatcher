package org.wryan67.vc.common;


import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by wryan on 5/21/2015.
 */
public class CookieJar extends HashMap<String,Cookie> {
    private Logger logger = Logger.getLogger(CookieJar.class);

    private final Logger ioLogger;

    public CookieJar(HttpServletRequest httpRequest, Logger ioLogger) {
        this.ioLogger = ioLogger;

        if (Util.logHeaders) {
            Enumeration x = httpRequest.getHeaderNames();



            for (String name : (ArrayList<String>)Collections.list(httpRequest.getHeaderNames())) {
                if (name != null) {
                    for (String value : (ArrayList<String>)Collections.list(httpRequest.getHeaders(name))) {
                        ioLogger.info(String.format("httpRequest.header: %s: %s", name, value));
                    }
                } else {
                    ioLogger.info(String.format("httpRequest.header: The header name is null"));
                }
            }
        }


        List<Cookie> cookies = getCookies(httpRequest);
        if (cookies==null) {
            return;
        }

        for (Cookie cookie:cookies) {
            put(cookie.getName(),cookie);
        }
    }

    public String getCookieValue(String cookieName) {
        Cookie cookie=this.get(cookieName);
        if (cookie==null) return null;
        return cookie.getValue();
    }

    public Long getCookieTimestamp(String cookieName) {
        Cookie cookie=this.get(cookieName);
        if (cookie==null) return 0L;

        try {
            return Long.parseLong(cookie.getValue());
        } catch (Exception e) {
            return 0L;
        }
    }


    private List<Cookie> getCookies(HttpServletRequest httpRequest) {
        ArrayList<Cookie> cookies=new ArrayList<>();

        if (httpRequest==null) {
            return cookies;
        }

        ArrayList<String> headerCookies=new ArrayList<>();

        for (String header: (ArrayList<String>)Collections.list(httpRequest.getHeaderNames())) {
            if (header!=null && header.trim().toLowerCase().equals("cookie")) {
                for (String cookie: (ArrayList<String>)Collections.list(httpRequest.getHeaders(header))) {
                    addIfExists(headerCookies, cookie);
                }
            }
        }

        for (String cookieHeader: headerCookies) {
            fillCookies(cookies,cookieHeader);
        }


        return cookies;
    }

    private void addIfExists(ArrayList<String> headerCookies, String cookie) {
        if (cookie!=null) {
            try {
                String decodedCookie=URLDecoder.decode(cookie,"UTF-8");
                headerCookies.add(decodedCookie);
            } catch (UnsupportedEncodingException e) {
                logger.error("failed to decode cookie: "+cookie,e);
            }
        }
    }

    public void ioLogger() {
        for (Cookie cookie:values()) {
            if (Util.logIO) ioLogger.info(String.format("[COOKIE] %s=%s", cookie.getName(), cookie.getValue()));
        }
    }

    private void fillCookies(ArrayList<Cookie> cookies, String cookieHeader) {
        if (cookieHeader==null) {
            return;
        }

        String cookiePairs[]=cookieHeader.split("[,;]");

        for (String cookiePair:cookiePairs) {
            String parts[]=cookiePair.trim().split("=");
            if (parts.length==2) {
                String name=parts[0].trim();
                String value=parts[1].trim();
                if (Util.logHeaders) ioLogger.info(String.format("httpRequest.cookie: name='%s' value='%s'",name,value));

                try {
                    cookies.add(new Cookie(name, value));
                } catch (Exception e) {
                    logger.warn("Cookie validation error",e);
                }
            }
        }
    }



}


