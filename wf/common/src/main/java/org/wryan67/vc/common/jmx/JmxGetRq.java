package org.wryan67.vc.common.jmx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="JmxGetRq")

public class JmxGetRq {
    @XmlElement(name = "Service", required = true)
	String service;
    
    @XmlElement(name = "Fields", required = true)
    String[] fields;
    
	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String[] getFields() {
		return fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}




}
