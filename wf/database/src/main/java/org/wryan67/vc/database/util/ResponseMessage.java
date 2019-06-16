package org.wryan67.vc.database.util;

public class ResponseMessage {
    int messageCode;
    String message;


    public ResponseMessage() {
        super();
    }
    public ResponseMessage(int messageCode, String message) {
        super();
        this.messageCode = messageCode;
        this.message = message;
    }
    public int getMessageCode() {
        return messageCode;
    }
    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}