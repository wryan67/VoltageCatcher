package org.wryan67.vc.common.jmx;


import org.apache.commons.lang3.ClassUtils;

import javax.management.ObjectName;
import java.util.List;

public class ServiceAttributes {
    private final Class instance;
    private final String serviceName;
    private final ObjectName objectName;
    private final Class mBeanClass;
    private final String configName;

    public ServiceAttributes(Class instance, String serviceName, ObjectName objectName, String configName) throws ClassNotFoundException {
        this.instance = instance;
        this.serviceName = serviceName;
        this.objectName = objectName;
        this.configName = configName;


        String mBeanClassName=instance.getName()+"MBean";




        Class mBeanClass=null;


        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(instance);

        for (Class c : interfaces) {
            if (c.getName().equals(mBeanClassName)) {
                mBeanClass=c;
                break;
            }
        }
        if (mBeanClass==null) {
            throw new RuntimeException("Cannot locate physical class for "+mBeanClassName);
        }
        this.mBeanClass=mBeanClass;


    }

    public Class getInstance() {
        return instance;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public Class getMBeanClass() {
        return mBeanClass;
    }

    public String getConfigName() {
        return configName;
    }
}
