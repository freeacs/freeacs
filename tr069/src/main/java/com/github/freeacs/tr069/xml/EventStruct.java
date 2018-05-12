package com.github.freeacs.tr069.xml;

public class EventStruct {
    public static final String ID = "EventStruct";
    
    private String eventCode;
    private String commandKey;
    
    public EventStruct() {
        this.eventCode = null;
        this.commandKey = null;
    }
    
    public EventStruct(String eventCode, String commandKey) {
        this.eventCode = eventCode;
        this.commandKey = commandKey;
    }

    public String getCommandKey() {
        return commandKey;
    }

    public void setCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }
   
}
