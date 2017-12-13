package com.kloudtek.mule.elogging.log4j2;

import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
import org.apache.logging.log4j.message.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MuleLogMessage implements Message {
    private HashMap<String,String> inboundProperties;
    private HashMap<String,String> outboundProperties;
    private HashMap<String,String> sessionProperties;
    private HashMap<String,String> flowVars;
    private String payloadClass;
    private String mimeType;
    private String encoding;
    private String payload;

    public MuleLogMessage(HashMap<String, String> inboundProperties, HashMap<String, String> outboundProperties, HashMap<String, String> sessionProperties, HashMap<String, String> flowVars, String payloadClass, String mimeType, String encoding, String payload) {
        this.inboundProperties = inboundProperties;
        this.outboundProperties = outboundProperties;
        this.sessionProperties = sessionProperties;
        this.flowVars = flowVars;
        this.payloadClass = payloadClass;
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.payload = payload;
    }

    @Override
    public String getFormattedMessage() {
        return toString();
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    public void toJson(ObjectComposer<JSONComposer<String>> json, String prefix) throws IOException {
        json.put(prefix+".content", this.payload)
                .put(prefix+".encoding",encoding)
                .put(prefix+".mimeType",mimeType)
                .put(prefix+".payloadClass",payloadClass);
        toJson(json,prefix+".inboundProperties",inboundProperties);
        toJson(json,prefix+".outboundProperties",outboundProperties);
        toJson(json,prefix+".sessionProperties",sessionProperties);
        toJson(json,prefix+".flowVars",flowVars);
    }

    private void toJson(ObjectComposer<JSONComposer<String>> json, String name, HashMap<String, String> map) throws IOException {
        if( ! map.isEmpty() ) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                json.put(name+"."+entry.getKey(),entry.getValue());
            }
        }
    }

    @Override
    public String toString() {
        return "MuleLogMessage{" +
                "inboundProperties=" + inboundProperties +
                ", outboundProperties=" + outboundProperties +
                ", sessionProperties=" + sessionProperties +
                ", flowVars=" + flowVars +
                ", payloadClass='" + payloadClass + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", encoding='" + encoding + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
