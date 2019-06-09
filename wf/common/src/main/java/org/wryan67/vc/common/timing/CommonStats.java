package org.wryan67.vc.common.timing;


import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.wryan67.vc.common.ADC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommonStats {
    private static final Logger logger=Logger.getLogger(CommonStats.class);
    private static final Logger tpsLogger=Logger.getLogger(CommonStats.class);
    private static final FastDateFormat logDateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss,SSS");


    private static final ConcurrentHashMap<String, LoadTestStats> globalStats =new ConcurrentHashMap<>();

    public static void add(String footprint, TimingStats stats) {
        ConcurrentHashMap<String,TimingStats> timings=(ConcurrentHashMap<String, TimingStats>) ADC.get("CommonStats");

        if (timings==null) {
            timings=new ConcurrentHashMap<String,TimingStats>();
            ADC.put("CommonStats", timings);

        }

        timings.put(footprint, stats);


        LoadTestStats loadStats=globalStats.putIfAbsent(footprint,new LoadTestStats(stats.getElapsedMS()));
        if (loadStats==null) {
            return;
        }
        loadStats.add(stats.getElapsedMS());



    }

    public static HashMap<String, LoadTestStats> initStats() {
        ArrayList<String> keys=Collections.list(globalStats.keys());
        HashMap<String,LoadTestStats> currentMap = new HashMap<>();

        Collections.sort(keys);

        for (String key: keys) {
            currentMap.put(key,globalStats.remove(key));
        }

        return currentMap;
    }

    private static Comparator<Map.Entry<String, TimingStats>> sortTimings = new Comparator<Map.Entry<String, TimingStats>>() {
        @Override
        public int compare(Map.Entry<String, TimingStats> a, Map.Entry<String, TimingStats> b) {
            long diff=(a.getValue().getEndNS()-b.getValue().getEndNS());
            if (diff<0) {
                return -1;
            }
            if (diff>0) {
                return 1;
            }
            return 0;
        }
    };


    public static void printCommonTimingStats(List<Map.Entry<String, TimingStats>> timings) {
        if (timings==null) return;

        String jbossid=(String)ADC.get("jbossid");
        StringBuffer out=new StringBuffer(256*timings.size());

        for (Map.Entry<String, TimingStats> e : timings) {
            String time=logDateFormat.format(e.getValue().getEndMS());

            out.append(String.format("[%s] %s INFO  CommonTiming::Exit: %s elapsed time %d [CommonTiming] [com.acxiom.ccsi.idmgmt.core.timing.CommonTiming](default task-0)\n",
                    jbossid, time, e.getKey(), e.getValue().getElapsedMS()));
        }
        tpsLogger.info("CommonStats\n"+out.toString());
    }

    public static List<Map.Entry<String, TimingStats>> getCommonTimingStats() {
        ConcurrentHashMap<String,TimingStats> timings=(ConcurrentHashMap<String, TimingStats>) ADC.get("CommonStats");

        if (timings==null) {
            tpsLogger.warn("no timing stats were gathered for this thread");
            return null;
        }

        List<Map.Entry<String, TimingStats>> sortedTimings = new ArrayList<>(timings.size());

        for (Map.Entry<String, TimingStats> e : timings.entrySet()) {
            sortedTimings.add(e);
        }

        try {
            Collections.sort(sortedTimings, sortTimings);
        } catch (IllegalArgumentException e) {
            logger.error("CommonStats sort error",e);
        }

        return sortedTimings;
    }



}
