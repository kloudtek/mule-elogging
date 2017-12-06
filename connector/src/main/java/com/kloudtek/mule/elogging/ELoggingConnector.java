package com.kloudtek.mule.elogging;

import com.kloudtek.mule.elogging.config.ConnectorConfig;
import com.kloudtek.mule.elogging.log4j2.MuleLogMessage;
import com.kloudtek.mule.elogging.log4j2.RequestResponseLogMessage;
import com.kloudtek.mule.elogging.util.ConnectorAnalyser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.api.MessagingException;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.NestedProcessor;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.transport.PropertyScope;
import org.mule.construct.Flow;
import org.mule.processor.chain.InterceptingChainLifecycleWrapper;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Connector(name = "elogging", friendlyName = "ELogging")
public class ELoggingConnector {
    public static final String SRC_EL = "Element";
    public static final String SRC_EL_XML = "Element XML";
    @Config
    ConnectorConfig config;
    private Logger logger;

    @Start
    public void init() {
        logger = LogManager.getLogger(config.getCategory());
    }

    /**
     * Log inbound payload request/response
     * <p>
     * param event mule event
     *
     * @return Payload
     */
    @Processor
    public Object logMessage(MuleEvent muleEvent) {
        MuleMessage message = muleEvent.getMessage();
        Level lvl = config.getLogLevel().getLvl();
        if (logger.isEnabled(lvl)) {
            logger.log(lvl, null, createMuleLogMessage(message));
        }
        return message.getPayload();
    }

    /**
     * Log inbound payload request/response
     *
     * @param nestedProcessor nested processor
     * @return payload
     */
    @Processor
    public Object logInbound(NestedProcessor nestedProcessor, MuleEvent muleEvent) throws Exception {
        return processAndLog(nestedProcessor, muleEvent, RequestResponseLogMessage.Type.INBOUND);
    }

    /**
     * Log outbound payload request/response
     *
     * @param nestedProcessor nested processor
     * @return payload
     */
    @Processor
    public Object logOutbound(NestedProcessor nestedProcessor, MuleEvent muleEvent) throws Exception {
        return processAndLog(nestedProcessor, muleEvent, RequestResponseLogMessage.Type.OUTBOUND);
    }

    private Object processAndLog(NestedProcessor nestedProcessor, MuleEvent muleEvent, RequestResponseLogMessage.Type logType) throws Exception {
        Level lvl = config.getLogLevel().getLvl();
        String flowName = null;
        String flowSourceFileName = null;
        String flowSourceFileLine = null;
        if (muleEvent.getFlowConstruct() instanceof Flow) {
            Flow flow = (Flow) muleEvent.getFlowConstruct();
            flowName = flow.getName();
            flowSourceFileName = objToString(flow.getAnnotation(new QName("http://www.mulesoft.org/schema/mule/documentation", "sourceFileName")));
            flowSourceFileLine = objToString(flow.getAnnotation(new QName("http://www.mulesoft.org/schema/mule/documentation", "sourceFileLine")));
        }
        String connectorClass = null;
        Map<String,String> connectorInfo = null;
        if( logType == RequestResponseLogMessage.Type.OUTBOUND) {
            MessageProcessor chain = ((PermissiveNestedProcessorChain) nestedProcessor).getChain();
            if( chain instanceof InterceptingChainLifecycleWrapper) {
                List<MessageProcessor> messageProcessors = ((InterceptingChainLifecycleWrapper) chain).getMessageProcessors();
                if( messageProcessors != null && ! messageProcessors.isEmpty() ) {
                    MessageProcessor backendMsgProcessor = messageProcessors.iterator().next();
                    connectorClass = backendMsgProcessor.getClass().getName();
                    connectorInfo = ConnectorAnalyser.analyse(backendMsgProcessor);
                }
            }
        }
        if (logger.isEnabled(lvl)) {
            MuleLogMessage req = createMuleLogMessage(muleEvent.getMessage());
            long start = System.currentTimeMillis();
            String messageSourceName = muleEvent.getMessageSourceName();
            String messageSourceNameUri = objToString(muleEvent.getMessageSourceURI());
            try {
                MuleMessage responseMessage = ((PermissiveNestedProcessorChain) nestedProcessor).process();
                MuleLogMessage resp = createMuleLogMessage(responseMessage);
                long duration = System.currentTimeMillis() - start;
                logger.log(lvl, null, new RequestResponseLogMessage(logType, req, resp, duration, messageSourceName, messageSourceNameUri, flowName, flowSourceFileName, flowSourceFileLine, connectorClass, connectorInfo));
                return responseMessage.getPayload();
            } catch (Exception e) {
                if (e instanceof MessagingException) {
                    long duration = System.currentTimeMillis() - start;
                    MuleLogMessage response = createMuleLogMessage(((MessagingException) e).getEvent().getMessage());
                    RequestResponseLogMessage rrmsg = new RequestResponseLogMessage(logType, req, response, duration, messageSourceName, messageSourceNameUri, flowName, flowSourceFileName, flowSourceFileLine, connectorClass, connectorInfo);
                    Map info = ((MessagingException) e).getInfo();
                    if (info.containsKey(SRC_EL)) {
                        rrmsg.setSourceElementLocation(info.get(SRC_EL).toString());
                    }
                    if (info.containsKey(SRC_EL_XML)) {
                        rrmsg.setSourceElementXml(info.get(SRC_EL_XML).toString());
                    }
                    logger.log(lvl, null, rrmsg);
                }
                throw e;
            }
        } else {
            return nestedProcessor.process();
        }
    }

    public ConnectorConfig getConfig() {
        return config;
    }

    public void setConfig(ConnectorConfig config) {
        this.config = config;
    }

    private MuleLogMessage createMuleLogMessage(MuleMessage muleMessage) {
        HashMap<String, String> inboundProperties = toMap(muleMessage, PropertyScope.INBOUND);
        HashMap<String, String> outboundProperties = toMap(muleMessage, PropertyScope.OUTBOUND);
        HashMap<String, String> sessionProperties = toMap(muleMessage, PropertyScope.SESSION);
        HashMap<String, String> flowVars = toMap(muleMessage, PropertyScope.INVOCATION);
        String encoding = muleMessage.getEncoding();
        String mimeType = null;
        if (muleMessage.getDataType() != null) {
            mimeType = muleMessage.getDataType().getMimeType();
        }
        String payload = null;
        String payloadClass = null;
        if (muleMessage.getPayload() != null) {
            payload = muleMessage.getPayloadForLogging();
            payloadClass = muleMessage.getPayload().getClass().getName();
        }
        return new MuleLogMessage(inboundProperties, outboundProperties, sessionProperties, flowVars, payloadClass, mimeType, encoding, payload);
    }

    private HashMap<String, String> toMap(MuleMessage message, PropertyScope propertyScope) {
        HashMap<String, String> map = new HashMap<>();
        for (String name : message.getPropertyNames(propertyScope)) {
            Object obj = message.getProperty(name, propertyScope);
            if (obj != null) {
                map.put(name, obj.toString());
            } else {
                map.put(name, null);
            }
        }
        return map;
    }

    public String objToString(Object obj) {
        return obj != null ? obj.toString() : null;
    }
}