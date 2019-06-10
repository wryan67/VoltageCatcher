package org.wryan67.vc.mbeans;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.wryan67.vc.common.Util;
import org.wryan67.vc.common.jmx.JMX;
import org.wryan67.vc.common.jmx.SimpleService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class Settings extends SimpleService implements SettingsMBean {
    public Boolean debug;


    private static Logger logger = Logger.getLogger(Settings.class);

    @PostConstruct
    public void register() {
        JMX.register(this);
        Util.debug = debug;

        if (!Util.debug) {
            Logger.getLogger("org").setLevel(Level.WARN);
            Logger.getLogger("akka").setLevel(Level.WARN);
            Logger.getLogger("kafka").setLevel(Level.WARN);
        }
    }

    @PreDestroy
    public void deregister() {
        JMX.deregister(this.getClass());
    }




    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
        Util.debug = debug;
    }
}
