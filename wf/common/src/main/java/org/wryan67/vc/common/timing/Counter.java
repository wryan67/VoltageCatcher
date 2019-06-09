package org.wryan67.vc.common.timing;

public class Counter {
    private long value;

    public Counter(long value) {
        this.value=value;
    }

    public long increment() {
        return ++value;
    }

    public long value() {
        return value;
    }

}
