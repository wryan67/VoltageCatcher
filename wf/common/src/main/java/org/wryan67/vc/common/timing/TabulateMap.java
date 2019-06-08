package org.wryan67.vc.common.timing;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedLong;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class TabulateMap extends ConcurrentHashMap<String, SynchronizedLong> {

    public Long increment(String key) {
        SynchronizedLong value=putIfAbsent(key, new SynchronizedLong(1));
        if (value==null) {
            return 1L;
        } else {
            return value.increment();
        }
    }

    public Long value(String key) {
        SynchronizedLong value=putIfAbsent(key, new SynchronizedLong(0));
        if (value==null) {
            return 0L;
        } else {
            return value.get();
        }
    }

    public String toString(String name) {
        int maxLen=0;
        for (String key: Collections.list(keys())) {
            if (key.length()>maxLen) {
                maxLen=key.length();
            }
        }

        StringBuilder out=new StringBuilder(132*size());
        out.append(name);
        out.append(" {\n");

        for (String key: Collections.list(keys())) {
            out.append(String.format("%-" + maxLen + "s %d\n",key,value(key)));
        }
        out.append("}\n");


        super.toString();
        return out.toString();
    }
}
