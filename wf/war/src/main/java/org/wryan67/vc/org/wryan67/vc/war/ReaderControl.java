package org.wryan67.vc.org.wryan67.vc.war;

import org.wryan67.vc.common.jmx.SimpleService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class ReaderControl  extends SimpleService {

    @PostConstruct
    public void register() {

    }

    @PreDestroy
    public void deregister() {
        VCReader.stopMonitor();
    }

}
