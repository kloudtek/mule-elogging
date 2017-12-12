package com.kloudtek.mule.ulogging.ulogging.config;

import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.param.Default;

@Configuration(friendlyName = "ULoggingConfiguration")
public class ULoggingConnectorConfig {
    /**
     * logAccess
     */
    @Configurable
    @Default("true")
    private boolean logAccess;

    /**
     * logAccess
     */
    @Configurable
    @Default("true")
    private boolean logPayloads;


    public boolean isLogAccess() {
        return logAccess;
    }

    public void setLogAccess(boolean logAccess) {
        this.logAccess = logAccess;
    }

    public boolean isLogPayloads() {
        return logPayloads;
    }

    public void setLogPayloads(boolean logPayloads) {
        this.logPayloads = logPayloads;
    }
}