package org.wryan67.vc.common.jmx;


import org.apache.log4j.Logger;
import org.wryan67.vc.common.Util;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

//import com.acxiom.ccsi.idmgmt.core.Logger;

public class JmxUtil {
	private static Logger logger = Logger.getLogger(JmxUtil.class);

	public JmxQueryRs query(JmxQueryRq request) {
		JmxQueryRs response=new JmxQueryRs();
		
		response.setStatusMessage("unknown error");
		response.setResultCount(-1L);
		
		if (Util.isBlankOrNull(request.getFilter())) {
			response.setStatusMessage("missing Filter in request");
			return response;
		}
		
		try {
	        TreeMap<String,ArrayList<MBeanData>> x = getDomainData(request.getFilter());
	        for (String domain:x.keySet()) {

	        	ArrayList<MBeanData> mbeanList=x.get(domain);
	        	for (MBeanData mbeanData:mbeanList) {
	        		String className=mbeanData.getInfo().getClassName()+"MBean";
		        	logger.info("service:"+mbeanData.getName()+" class:"+className);
		        	response.getServices().add(mbeanData.getName().getCanonicalName());
	        	}
	        	response.setResultCount(0L+response.getServices().size());
	        	response.setStatusMessage("success");
	        }
		} catch (MalformedObjectNameException e) {
			logger.error("MalformedObjectNameException",e);

		} catch (NullPointerException e) {
			logger.error("NullPointerException",e);
		} catch (JMException e) {
			logger.error("JMException",e);
		}
		
		return response;
	}
	
    public static TreeMap<String,ArrayList<MBeanData>>   getDomainData(String   filter) throws JMException  {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      TreeMap<String,ArrayList<MBeanData>>   domainData = new TreeMap<String,ArrayList<MBeanData>>();
      if( server != null )
      {
         ObjectName   filterName = null;
         if( filter != null )
            filterName = new ObjectName  (filter);
         Set   objectNames = server.queryNames(filterName, null);
         Iterator   objectNamesIter = objectNames.iterator();
         while( objectNamesIter.hasNext() )
         {
            ObjectName   name = (ObjectName  ) objectNamesIter.next();
            MBeanInfo   info = server.getMBeanInfo(name);
            String   domainName = name.getDomain();
            MBeanData mbeanData = new MBeanData(name, info);
            //logger.info("mbeanData:"+mbeanData.getName());
            ArrayList<MBeanData> data =  (ArrayList<MBeanData>) domainData.get(domainName);
            if( data == null )
            {
               data = new ArrayList<MBeanData>();
               domainData.put(domainName, data);
            }
            data.add(mbeanData);
         }
      }
//      Iterator   domainDataIter = domainData.values().iterator();
      return domainData;
   }

//	public JmxGetRs get(JmxGetRq request) {
//		JmxGetRs response = new JmxGetRs();
//		response.setStatusMessage("unknown error");
//		response.setResultCount(-1L);
//		boolean warn=false;
//
//		Object mbean=Settings.getMBean(request.getService(), ServiceMBean.class);
//
//
//		logger.info("mbean="+mbean.getClass().getCanonicalName());
//
//		Method[] methods = mbean.getClass().getMethods();
//		for (Method m : methods) {
//			logger.info("mbean method: "+m.getName());
//		}
//
//		MBeanInfo mbeanInfo = null;
//		try {
//			mbeanInfo = (MBeanInfo) mbean.getClass().getMethod("getMBeanInfo").invoke(mbean);
//
//
//			Class<?> c = Class.forName(mbeanInfo.getClassName()+"MBean");
//
//			mbean=Settings.getMBean(request.getService(),c);
//			logger.info("mbean="+mbean.getClass().getName());
//
//			for (String field: request.getFields()) {
//				try {
//					Method m=c.getMethod("get"+field);
//					Object v=m.invoke(mbean);
//					response.getFields().put(field, v.toString());
//					logger.info(field+"="+v.toString());
//					response.setResultCount(response.getFields().size()+0L);
//				} catch (NoSuchMethodException e) {
//					logger.warn(mbeanInfo.getClassName()+" method=get"+field+" "+e.getMessage());
//					warn=true;
//				}
//			}
//			if (warn) {
//				response.setStatusMessage("Some fields could not be retreived");
//			} else {
//				response.setStatusMessage("success");
//			}
//
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		logger.info("mbean class="+mbeanInfo.getClassName());
//		logger.info("response size="+response.getFields().size());
//		for (String k: response.getFields().keySet()) {
//			logger.info("response.field::"+k+"="+response.getFields().get(k));
//		}
//
//		return response;
//	}



}
