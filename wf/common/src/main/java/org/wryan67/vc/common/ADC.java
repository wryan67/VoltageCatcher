package org.wryan67.vc.common;

import java.util.concurrent.ConcurrentHashMap;

public class ADC {

    private static ThreadLocal systemMap = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new ConcurrentHashMap<String,Object>();
        }
    };


    public static Object get(String key) {
        ConcurrentHashMap<String,Object> threadMap= (ConcurrentHashMap<String,Object>)systemMap.get();
        return threadMap.get(key);
    }

    public static void put(String key, Object value) {
        ConcurrentHashMap<String,Object> threadMap= (ConcurrentHashMap<String,Object>)systemMap.get();

        threadMap.put(key, value);
    }

    public static void clear() {
        ConcurrentHashMap<String,Object> threadMap= (ConcurrentHashMap<String,Object>)systemMap.get();

        threadMap.clear();

    }


    public static String values() {
        ConcurrentHashMap<String,Object> threadMap= (ConcurrentHashMap<String,Object>)systemMap.get();
        return "ADC Thread Local values:  "+threadMap.toString();
    }
}
