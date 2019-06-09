package org.wryan67.vc.common.jmx;

import javax.management.MBeanInfo;
import javax.management.ObjectName;

/**
 * Created by wryan on 3/23/2016.
 */
public class MBeanData {
    private ObjectName name;
    private MBeanInfo info;

    public MBeanData(ObjectName name, MBeanInfo info) {
        this.name=name;
        this.info=info;
    }

    public ObjectName getName() {
        return name;
    }

    public void setName(ObjectName name) {
        this.name = name;
    }

    public MBeanInfo getInfo() {
        return info;
    }

    public void setInfo(MBeanInfo info) {
        this.info = info;
    }
}
