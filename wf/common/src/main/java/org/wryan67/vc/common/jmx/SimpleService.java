package org.wryan67.vc.common.jmx;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by wryan on 6/22/2016.
 */
public abstract class SimpleService {
    // mbean stuff

    @JsonIgnore
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @JsonIgnore
    public int getState() {
        return 0;
    }

    @JsonIgnore
    public String getStateString() {
        return "active";
    }

    public void jbossInternalLifecycle(String var1) throws Exception {

    }

    public void create() throws Exception {

    }

    public void start() throws Exception {

    }

    public void stop() {

    }

    public void destroy() {

    }
}
