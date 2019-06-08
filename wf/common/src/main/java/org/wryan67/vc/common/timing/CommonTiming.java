package org.wryan67.vc.common.timing;


import org.apache.log4j.Logger;
import org.wryan67.vc.common.ADC;
import org.wryan67.vc.common.Util;

import java.util.HashMap;

public class CommonTiming {
    private static Logger logger = Logger.getLogger(CommonTiming.class);

    private HashMap<String, TimingStats> startTimes=  new HashMap<>();


    

    public static boolean start() {
        String method=getMethodName("start");
        return start(method);
    }


    public static boolean start(String method, String parm) {
        CommonTiming timings=null;
        try {
            timings = (CommonTiming) ADC.get("timings");
        }catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        if (timings==null) timings=init();

        if (parm==null) {
            return timings.startTiming(method);
        } else {
            return timings.startTiming(method+parm);
        }
    }

    public static boolean start(String method) {
        CommonTiming timings=null;
        try {
            timings = (CommonTiming) ADC.get("timings");
        }catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        if (timings==null) timings=init();

        return timings.startTiming(method);
    }


    public static void log(TimingStats stats, String method) {
        log(stats,method, (String[]) null);
    }

    public static void log(TimingStats stats, String method, String parm) {
        if (parm==null) {
            log(stats,method, (String[]) null);
        } else {
            String[] parms = new String[1];
            parms[0]=parm;

            log(stats,method,parms);
        }
    }

    public static void log(TimingStats stats, String method, String[] parms) {
        if (ADC.get("service")!=null && ADC.get("service").equals("sanityCheck")) return;

        int len=method.length()+4;
        if (parms!=null) {
            len+=parms.length*16;
        }

        StringBuilder footprintBuf=new StringBuilder(len);
        footprintBuf.append(method);
        footprintBuf.append("(");
        if (parms!=null) {
            for (int i=0;i<parms.length-1;++i) {
                footprintBuf.append(parms[i]); footprintBuf.append(",");
            }   footprintBuf.append(parms[parms.length-1]);
        }
        footprintBuf.append(")");

        String footprint=footprintBuf.toString();

        CommonStats.add(footprint,stats);

//        if (!Util.logCommonTiming) return;
//
//        StringBuilder buf=new StringBuilder(128);
//        buf.append("CommonTiming::Exit: ");
//        buf.append(footprint);
//        buf.append(" elapsed time ");
//        buf.append(elapsed);
//
//        logger.info(buf.toString());
    }

    public static void logInstant(TimingStats stats, String method, String... parms) {

        int len=method.length()+4;
        if (parms!=null) {
            len+=parms.length*16;
        }

        StringBuilder footprintBuf=new StringBuilder(len);
        footprintBuf.append(method);
        footprintBuf.append("(");
        if (parms!=null) {
            for (int i=0;i<parms.length-1;++i) {
                footprintBuf.append(parms[i]); footprintBuf.append(",");
            }   footprintBuf.append(parms[parms.length-1]);
        }
        footprintBuf.append(")");
        
        String footprint=footprintBuf.toString();


        if (!Util.logCommonTiming) return;

        StringBuilder buf=new StringBuilder(128);
        buf.append("CommonTiming::Exit: ");
        buf.append(footprint);
        buf.append(" elapsed time ");
        buf.append(stats.getElapsedMS());

        logger.info(buf.toString());
    }

    private static String getMethodName(String me) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        int i=0;
        while (!(stack[i]).getMethodName().equals(me)) {
            ++i;
        }
        return stack[++i].getMethodName();
    }

    private static CommonTiming init() {
        CommonTiming timings=new CommonTiming();
        ADC.put("timings", (CommonTiming) timings);
        return timings;
    }
    public static boolean stop() {
        String method = getMethodName("stop");
        return stop(method);
    }
    public static boolean stop(String method) {
        CommonTiming timings=(CommonTiming) ADC.get("timings");
        if (timings==null) timings=init();

        return timings.stop(method, true, null);
    }

    public static boolean stop(String method, String parm) {
        CommonTiming timings=(CommonTiming) ADC.get("timings");
        if (timings==null) timings=init();

        return timings.stop(method, true, parm);
    }


    private boolean startTiming(String method) {
        TimingStats startTime = startTimes.get(method);
//        if (force) {
//            elapsedTimes.remove(method);
//        }
        if (startTime==null) {
            startTimes.put(method,new TimingStats());
            return true;
        } else {
            if (!Util.loadtest) {
                logger.error("timings::startTiming: method " + method + "() has already been started or used");
            }
            return false;
        }
    }


    private boolean stopTiming(String method) {
        return stop(method,true, null);
    }

    private boolean stop(String userMethod, boolean log, String parm) {
        String method=(parm==null)?userMethod:userMethod+parm;

        TimingStats startTime = startTimes.get(method);
        if (startTime!=null) {
            CommonTiming.log(startTime.end(), userMethod,parm);
            startTimes.remove(method);
            return true;
        } else {
            if (!Util.loadtest) {
                logger.error("could not find startTiming time for method: " + userMethod);
            }
            return false;
        }
    }


    public static void log(TimingStats timingStats, String method, String parm,  Logger tpsLogger) {
        StringBuilder buf=new StringBuilder(128);
        buf.append("CommonTiming::Exit: ");
        buf.append(method);
        buf.append("(");
        if (parm!=null) buf.append(parm);
        buf.append(") elapsed time ");
        buf.append(timingStats.getElapsedMS());
        tpsLogger.info(buf.toString());
    }
}
