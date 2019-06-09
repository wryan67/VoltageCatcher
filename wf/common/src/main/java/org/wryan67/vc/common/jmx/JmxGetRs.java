package org.wryan67.vc.common.jmx;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="JmxGetRs")

public class JmxGetRs {
    @XmlElement(name = "StatusMessage", required = true)
	private String statusMessage;
	
    @XmlElement(name = "ResultCount", required = true)
	private Long resultCount;

//    @XmlElement(name = "Fields", required = false, type=java.util.HashMap.class)
	private HashMap<String,String> Fields;

    
	public HashMap<String, String> getFields() {
		if (Fields==null) Fields=new HashMap<String,String>();
		return Fields;
	}

	public void setFields(HashMap<String, String> fields) {
		this.Fields = fields;
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
