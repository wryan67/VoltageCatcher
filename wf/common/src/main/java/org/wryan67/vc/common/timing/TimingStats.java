package org.wryan67.vc.common.timing;


public class TimingStats {
    private long startNS=System.nanoTime();
    private long startMS=System.currentTimeMillis();
    private long endNS=-1;
    private long endMS=-1;
    private long elapsedNS=-1;
    private long elapsedMS=-1;

    public TimingStats() {}

    public long getStartNS() {
        return startNS;
    }
    public long getStartMS() {
        return startMS;
    }
    public long getEndNS() {
        return endNS;
    }
    public long getEndMS() {
        return endMS;
    }
    public long getElapsedNS() {
        return elapsedNS;
    }
    public long getElapsedMS() {
        return elapsedMS;
    }

    public TimingStats end() {
        endNS = System.nanoTime();
        endMS = System.currentTimeMillis();

        elapsedNS = endNS - startNS;
        elapsedMS = endMS - startMS;

        return this;
    }


    @Override
    public String toString() {
        return "TimingStats{" +
                "startNS=" + startNS +
                ", startMS=" + startMS +
                ", endNS=" + endNS +
                ", endMS=" + endMS +
                ", elapsedNS=" + elapsedNS +
                ", elapsedMS=" + elapsedMS +
                '}';
    }
}


