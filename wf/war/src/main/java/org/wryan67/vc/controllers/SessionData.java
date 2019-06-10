package org.wryan67.vc.controllers;

import javax.servlet.http.HttpServletRequest;



public class SessionData {

    public enum SessionVar {
        userMessage,
        file2download,
        status,
        userOptions;
    }


    public static <Type> Type getValue(HttpServletRequest request, SessionVar name) {
        return (Type)request.getSession().getAttribute(name.toString());
    }

    public static boolean exists(HttpServletRequest request, SessionVar name) {
        return (request.getSession().getAttribute(name.toString())==null)?false:true;
    }




    public static <Type> Type getValueOrDefault(HttpServletRequest request, SessionVar name, Type newValue) {
        Object value=request.getSession().getAttribute(name.toString());
        if (value==null) {
            request.getSession().setAttribute(name.toString(),newValue);
            return newValue;
        } else {
            return (Type) value;
        }
    }


    public static <Type> Type getValueAndRemove(HttpServletRequest request, SessionVar name) {
        Type rs=getValue(request, name);

        remove(request,name);
        return rs;
    }

    public static void remove(HttpServletRequest request, SessionVar name) {
        request.getSession().removeAttribute(name.toString());
    }


    public static void setValue(HttpServletRequest request, SessionVar name, Object value) {
        request.getSession().setAttribute(name.toString() ,value);
    }

}
