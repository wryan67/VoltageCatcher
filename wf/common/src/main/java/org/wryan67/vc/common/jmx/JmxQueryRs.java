package org.wryan67.vc.common.jmx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="JmxQueryRs")

public class JmxQueryRs {
    @XmlElement(name = "StatusMessage", required = true)
	private String statusMessage;
	
    @XmlElement(name = "ResultCount", required = true)
	private Long resultCount;

    @XmlElement(name = "Services", required = true)
	private ArrayList<String> services;

    
	public void setServices(ArrayList<String> services) {
		this.services = services;
	}

	public ArrayList<String> getServices() {
		if (services==null) services=new ArrayList<String>();
		return services;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public Long getResultCount() {
		return resultCount;
	}

	public void setResultCount(Long messageCount) {
		this.resultCount = messageCount;
	}
    

}
