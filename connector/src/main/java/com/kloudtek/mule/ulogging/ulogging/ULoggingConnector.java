package com.kloudtek.mule.ulogging.ulogging;

import org.mule.api.NestedProcessor;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;

import com.kloudtek.mule.ulogging.ulogging.config.ConnectorConfig;

@Connector(name="ulogging", friendlyName="ULogging")
public class ULoggingConnector {
    @Config
    ConnectorConfig config;

    /**
     * Log inbound payload request/response
     * @param nestedProcessor nested processor
     * @return payload
     */
    @Processor
    public Object logInbound( NestedProcessor nestedProcessor) {
        return null;
    }

    /**
     * Log outbound payload request/response
     * @param nestedProcessor nested processor
     * @return payload
     */
    @Processor
    public Object logOutbound(NestedProcessor nestedProcessor) {
        return null;
    }

    public ConnectorConfig getConfig() {
        return config;
    }

    public void setConfig(ConnectorConfig config) {
        this.config = config;
    }

}