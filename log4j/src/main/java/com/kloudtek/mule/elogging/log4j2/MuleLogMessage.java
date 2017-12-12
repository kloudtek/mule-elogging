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
        return null;
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

    public void toJson(ObjectComposer<JSONComposer<String>> json, String objName) throws IOException {
        ObjectComposer<ObjectComposer<JSONComposer<String>>> jsonObj = json.startObjectField(objName);
        jsonObj.put("message","mule message logged").put("content", this.payload).put("encoding",encoding)
                .put("mimeType",mimeType).put("payloadClass",payloadClass);
        toJson(jsonObj,"inboundProperties",inboundProperties);
        toJson(jsonObj,"outboundProperties",outboundProperties);
        toJson(jsonObj,"sessionProperties",sessionProperties);
        toJson(jsonObj,"flowVars",flowVars);
        jsonObj.end();
    }

    private void toJson(ObjectComposer<ObjectComposer<JSONComposer<String>>> json, String name, HashMap<String, String> map) throws IOException {
        if( ! map.isEmpty() ) {
            json.startObjectField(name);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                json.put(entry.getKey(),entry.getValue());
            }
            json.end();
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
