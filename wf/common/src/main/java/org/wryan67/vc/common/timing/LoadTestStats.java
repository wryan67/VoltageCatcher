package org.wryan67.vc.common.timing;


import java.util.*;

public class LoadTestStats {
    private long    n=0;              // Total number of transactions
    private long    sum=0;            // Running sum of elapsed times.
    private long    sum2=0;           // Running sum of elapsed time squared.

    private HashMap<Long, Counter> elapsedMap=new HashMap<>();  // key=elapsed t1ime  value=frequency of elapsed time

    public LoadTestStats() {

    }

    public LoadTestStats(long elapsed) {
        add(elapsed);
    }

    public synchronized void add(long elapsed) {
        ++n;
        sum+=elapsed;
        sum2+=elapsed*elapsed;

        Counter stat=elapsedMap.get(elapsed);
        if (stat==null) {
            elapsedMap.put(elapsed,new Counter(1));
        } else {
            stat.increment();
        }
    }

    public synchronized double standardDeviation() {
        double mean=(double)sum/n;
        return Math.sqrt(((double)sum2/n) - (mean*mean));
    }

    public synchronized Long percentile(double percent) {
        long count = 0;

        List<Long> keys = new ArrayList<>(elapsedMap.keySet());
        Collections.sort(keys);

        for (Long i : keys) {
            count += elapsedMap.get(i).value();
            if (100.0 * count / n >= percent) {
                return i;
            }
        }
        return -1L;
    }

    public double mean() {
        return (double)sum/n;
    }

    public long getN() {
        return n;
    }

    public long getSum() {
        return sum;
    }

    public long getSum2() {
        return sum2;
    }

}


