package org.wryan67.vc.database.util;

import java.util.ArrayList;

public class ResponseStatus {
    int status;
    String message;
    ArrayList<ResponseMessage> messages;

    public ResponseStatus() {
        super();
    }
    public ResponseStatus(int status, String message,
                          ArrayList<ResponseMessage> messages) {
        super();
        this.status = status;
        this.message = message;
        this.messages = messages;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public ArrayList<ResponseMessage> getMessages() {
        if (messages==null) messages=new ArrayList<ResponseMessage>();
        return messages;
    }
    public void setMessages(ArrayList<ResponseMessage> messages) {
        this.messages = messages;
    }
    public void addMessage(int s, String m) {
        if (messages==null) {
            setMessages(new ArrayList<ResponseMessage>());
        }
        ResponseMessage message=new ResponseMessage(s, m);
        messages.add(message);
    }

}
