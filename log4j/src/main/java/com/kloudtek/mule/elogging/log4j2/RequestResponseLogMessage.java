package com.kloudtek.mule.elogging.log4j2;

import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
import org.apache.logging.log4j.message.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestResponseLogMessage implements Message {
    private Type type;
    private MuleLogMessage request;
    private MuleLogMessage response;
    private String sourceElementLocation;
    private String sourceElementXml;
    private String messageSourceUri;
    private String messageSourceName;
    private String flowName;
    private String flowFileName;
    private String flowFileLine;
    private String connectorClass;
    private Map<String,String> connectorInfo;
    private long duration;

    public RequestResponseLogMessage(Type type, MuleLogMessage request, MuleLogMessage outboundMessage, long duration, String messageSourceUri, String messageSourceName,String flowName, String flowFileName, String flowFileLine, String connectorClass, Map<String,String> connectorInfo ) {
        this.type = type;
        this.request = request;
        this.response = outboundMessage;
        this.duration = duration;
        this.messageSourceUri = messageSourceUri;
        this.messageSourceName = messageSourceName;
        this.flowName = flowName;
        this.flowFileName = flowFileName;
        this.flowFileLine = flowFileLine;
        this.connectorClass = connectorClass;
        this.connectorInfo = connectorInfo;
    }

    public void toJson(ObjectComposer<JSONComposer<String>> json) throws IOException {
        json.put("type",type.name().toLowerCase());
        json.put("message",type.name().toLowerCase()+" mule message");
        if( request != null ) {
            request.toJson(json,"request");
        }
        if( response != null ) {
            response.toJson(json,"response");
        }
        if( sourceElementLocation != null ) {
            json.put("sourceElementLocation",sourceElementLocation);
        }
        if( sourceElementXml != null ) {
            json.put("sourceElementXml",sourceElementXml);
        }
        json.put("messageSourceUri",messageSourceUri);
        json.put("messageSourceName",messageSourceUri);
        json.put("duration",duration);
        json.put("flowName",flowName);
        json.put("flowFileName",flowFileName);
        json.put("flowFileLine",flowFileLine);
        if( connectorClass != null ) {
            ObjectComposer<ObjectComposer<JSONComposer<String>>> con = json.startObjectField("connector");
            json.put("class",connectorClass);
            if( connectorInfo != null ) {
                for (Map.Entry<String, String> entry : connectorInfo.entrySet()) {
                    json.put(entry.getKey(),entry.getValue());
                }
            }
            con.end();
        }
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

    public String getSourceElementLocation() {
        return sourceElementLocation;
    }

    public void setSourceElementLocation(String sourceElementLocation) {
        this.sourceElementLocation = sourceElementLocation;
    }

    public String getSourceElementXml() {
        return sourceElementXml;
    }

    public void setSourceElementXml(String sourceElementXml) {
        this.sourceElementXml = sourceElementXml;
    }

    public String getMessageSourceUri() {
        return messageSourceUri;
    }

    public void setMessageSourceUri(String messageSourceUri) {
        this.messageSourceUri = messageSourceUri;
    }

    public String getMessageSourceName() {
        return messageSourceName;
    }

    public void setMessageSourceName(String messageSourceName) {
        this.messageSourceName = messageSourceName;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowFileName() {
        return flowFileName;
    }

    public void setFlowFileName(String flowFileName) {
        this.flowFileName = flowFileName;
    }

    public String getFlowFileLine() {
        return flowFileLine;
    }

    public void setFlowFileLine(String flowFileLine) {
        this.flowFileLine = flowFileLine;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        INBOUND, OUTBOUND
    }

    @Override
    public String toString() {
        return "RequestResponseLogMessage{" +
                "type=" + type +
                ", request=" + request +
                ", response=" + response +
                ", sourceElementLocation='" + sourceElementLocation + '\'' +
                ", sourceElementXml='" + sourceElementXml + '\'' +
                ", messageSourceUri='" + messageSourceUri + '\'' +
                ", messageSourceName='" + messageSourceName + '\'' +
                ", flowName='" + flowName + '\'' +
                ", flowFileName='" + flowFileName + '\'' +
                ", flowFileLine='" + flowFileLine + '\'' +
                ", connectorClass='" + connectorClass + '\'' +
                ", connectorInfo=" + connectorInfo +
                ", duration=" + duration +
                '}';
    }
}
