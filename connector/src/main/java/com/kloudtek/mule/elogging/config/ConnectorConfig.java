package com.kloudtek.mule.elogging.config;

import com.kloudtek.mule.elogging.LogLevel;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.param.Default;

@Configuration(friendlyName = "Configuration")
public class ConnectorConfig {
    @Configurable
    @Default("mulepayload")
    private String category;

    @Configurable
    @Default("DEBUG")
    private LogLevel logLevel;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }
}