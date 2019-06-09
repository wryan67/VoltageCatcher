package org.wryan67.vc.common.jmx;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.wryan67.vc.common.FileUtil;

import javax.management.*;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wryan on 9/16/2015.
 */
public class JMX {
    private static final Logger logger = Logger.getLogger(JMX.class);
    private static MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();


    private static ConcurrentHashMap<Class,ServiceAttributes> services=new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Class,ServiceAttributes> serviceMBeans=new ConcurrentHashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static void loadConfig(Object instance, String configName) {
        String configFileName="../config/" + configName + ".cfg";

        File configFile=new File(configFileName);

        if (configFile.exists()) {
            try {
                mapper.readerForUpdating(instance).readValue(FileUtil.fileString(configFileName, logger));
            } catch (IOException e) {
                logger.error("load config failed to read file ("+configFileName+")", e);
            }
        } else {
            logger.error("config file ("+configFileName+"), does not exist!");
        }
    }

    public static void saveConfig(Class mBean) throws IOException {
        ServiceAttributes service=getServiceAttributes(mBean);

        String configName=service.getConfigName();
        String configFileName="../config/" + configName + ".cfg";

        Object bean=JMX.getMBean(service.getObjectName(),service.getMBeanClass());

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(configFileName),bean);
        } catch (IOException e) {
            String msg="Unable to save configuration for "+mBean.getSimpleName();
            logger.error(msg,e);
            throw new IOException(msg,e);
        }
    }

    private static ServiceAttributes getServiceAttributes(Class mBean) {
        ServiceAttributes service=serviceMBeans.get(mBean);
        if (service==null) {
            service=services.get(mBean);
        }
        if (service==null) {
            logServices();
            throw new  IllegalStateException("Cannot locate service for "+mBean.getSimpleName());
        }

        return service;
    }

    private static void logServices() {
        logger.info("Registered Services:");

        for (Map.Entry<Class, ServiceAttributes> e : services.entrySet()) {
            ServiceAttributes v = e.getValue();
            logger.info("Class="+e.getKey().getSimpleName()+" ConfigName="+v.getServiceName()+" mBean="+v.getMBeanClass().getSimpleName());
        }
    }


    public static void register(Object instance) {
        String configName=instance.getClass().getSimpleName();
        String packageName=instance.getClass().getPackage().getName();
        String serviceName=packageName + ":service=" + configName;

        loadConfig(instance, configName);
        try {
            JMX.register(serviceName, instance, configName);
        } catch (InstanceAlreadyExistsException e) {
            throw new RuntimeException("problem registering "+serviceName,e);
        }
    }

    private static void register(String serviceName, Object instance, String configName) throws InstanceAlreadyExistsException {
        try {
            logger.info("Registering " + serviceName);
            ObjectName objectName = new ObjectName(serviceName);
            platformMBeanServer.registerMBean(instance, objectName);
            logger.info("Registered " + serviceName);


            ServiceAttributes service=new ServiceAttributes(instance.getClass(), serviceName, objectName, configName);
            services.put(instance.getClass(),service);
            serviceMBeans.put(service.getMBeanClass(),service);
        } catch (InstanceAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Problem during registration of ("+serviceName+") into JMX:" + e.getMessage(),e);
        }
    }

    public static void deregister(Class instance) {
        if (instance==null) {
            throw new IllegalStateException("Problem during de-registration of mbean, instance cannot be null");
        }
        ServiceAttributes service=getServiceAttributes(instance);
        try {
            platformMBeanServer.unregisterMBean(service.getObjectName());
            serviceMBeans.remove(service.getMBeanClass());
            services.remove(service.getInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Problem during de-registration of mbean ("+service.getMBeanClass().getSimpleName()+") from JMX:"+e.getMessage(), e);
        }
    }
    public static Object getMBean(String name, Class mbeanClass) {
        long curtime=System.currentTimeMillis();
        Object mbean=null;
        while (mbean==null && (System.currentTimeMillis()-curtime)<60000) {
            try {
                ObjectName service = new ObjectName(name);
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                mbean=javax.management.JMX.newMBeanProxy(server, service, mbeanClass);
            } catch (Exception e) {
                String msg=e.getMessage();
                if (msg.contains("is not registered")) {
                    logger.warn(msg);
                    try {
                        Thread.sleep(333);
                    } catch (InterruptedException e2) {
                        logger.info("interrupt");
                    }
                } else {
                    logger.error("Exception caught in init: ",e);
                    break;
                }
            }
        }
        if (mbean==null) {
            logger.error("cannot locate mbean "+name);
        }
        return mbean;
    }

    public static Object getMBean(ObjectName service, Class mbeanClass) {

        long curtime=System.currentTimeMillis();
        Object mbean=null;
        while (mbean==null && (System.currentTimeMillis()-curtime)<60000) {
            try {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();


                mbean= javax.management.JMX.newMBeanProxy(server, service, mbeanClass);
//                mbean = MBeanProxy.get(mbeanClass, service, server);
            } catch (Exception e) {
                String msg=e.getMessage();
                if (msg.contains("is not registered")) {
                    logger.info(msg);
                    try {
                        Thread.sleep(333);
                    } catch (InterruptedException e2) {
                        logger.info("interrupt");
                    }
                } else {
                    logger.error("Exception caught in init: ",e);
                    break;
                }
            }
        }
        if (mbean==null) {
            logger.error("cannot locate mbean "+service.getCanonicalName());
        }
        return mbean;
    }


    public static <MBean> MBean getMBean(Class<MBean> mBeanClass) {
        ServiceAttributes service=getServiceAttributes(mBeanClass);

        return (MBean) getMBean(service.getObjectName(),service.getMBeanClass());
    }




    public static ArrayList<Object> getMBeans(String filter) {
        ArrayList<Object> mbeans = new ArrayList<Object>();
        try {
            TreeMap<String,ArrayList<MBeanData>> x = getDomainData(filter);
            for (String domain:x.keySet()) {

                ArrayList<MBeanData> mbeanList=x.get(domain);
                for (MBeanData mbeanData:mbeanList) {
                    String className=mbeanData.getInfo().getClassName()+"MBean";
                    Class mbeanClass=Class.forName(className);
                    Object mbean = getMBean(mbeanData.getName(),mbeanClass);
                    mbeans.add(mbean);
                }
            }
        } catch (MalformedObjectNameException e) {
            logger.error("MalformedObjectNameException",e);
        } catch (NullPointerException e) {
            logger.error("NullPointerException",e);
        } catch (JMException e) {
            logger.error("JMException",e);
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException",e);
        }
        return mbeans;
    }


    public static TreeMap<String,ArrayList<MBeanData>>   getDomainData(String   filter) throws JMException  {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        TreeMap<String,ArrayList<MBeanData>>   domainData = new TreeMap<String,ArrayList<MBeanData>>();
        if( server != null )
        {
            ObjectName   filterName = null;
            if( filter != null )
                filterName = new ObjectName  (filter);
            Set objectNames = server.queryNames(filterName, null);

//            logger.info("domain data filter="+filter+", found "+objectNames.size()+" mbeans");

            Iterator objectNamesIter = objectNames.iterator();
            while( objectNamesIter.hasNext() )
            {
                ObjectName   name = (ObjectName  ) objectNamesIter.next();
                MBeanInfo info = server.getMBeanInfo(name);
                String   domainName = name.getDomain();
                MBeanData mbeanData = new MBeanData(name, info);
//                logger.info("mbeanData:"+mbeanData.getName());
                ArrayList<MBeanData> data = domainData.get(domainName);
                if( data == null )
                {
                    data = new ArrayList();
                    domainData.put(domainName, data);
                }
                data.add(mbeanData);
            }
        }
        return domainData;
    }

}
