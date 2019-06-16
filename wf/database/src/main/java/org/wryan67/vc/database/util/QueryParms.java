package org.wryan67.vc.database.util;


import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.HashMap;

public class QueryParms extends HashMap<String, Object> {
    private static final Logger logger = Logger.getLogger(QueryParms.class);

    public String matchClause(String className) {
        Class tableClass=null;
        String lookup= dbCaller.dbPackage+"."+className;
        try {
            tableClass = Class.forName(lookup);
        } catch (ClassNotFoundException e) {
            logger.error("Cannot locate table class ("+lookup+") for table "+className);
            return null;
        }
        StringBuilder sb=new StringBuilder(1024);

        sb.append(" ");
        int count=0;
        for (String key:this.keySet()) {
            if (count++>0) sb.append("AND ");
            sb.append(key);
            sb.append(" = ");
            try {
                Method getter = tableClass.getMethod("get"+key);
                String type=getter.getReturnType().getName();
                boolean useQuotes=true;
                if (type.equals("java.lang.Integer")) 		useQuotes=false;
                if (type.equals("java.lang.Long")) 			useQuotes=false;
                if (type.equals("java.math.BigDecimal")) 	useQuotes=false;
                if (type.equals("java.math.BigInteger")) 	useQuotes=false;
                if (useQuotes) {
                    sb.append("'");
                }
                sb.append(this.get(key));
                if (useQuotes) {
                    sb.append("'");
                }
                sb.append(" ");


            } catch (SecurityException e) {			logger.error("SecurityException",e);
            } catch (NoSuchMethodException e) {		logger.error("NoSuchMethodException",e);
            }
        }


        return sb.toString();
    }
}
