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

    @Configurable
    @Default("true")
    private boolean addTransactionId;

    @Configurable
    @Default("mule_tx_id")
    private String transactionIdName;

    @Configurable
    @Default("true")
    private boolean acceptExternalTransactionId;

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

    public boolean isAddTransactionId() {
        return addTransactionId;
    }

    public void setAddTransactionId(boolean addTransactionId) {
        this.addTransactionId = addTransactionId;
    }

    public String getTransactionIdName() {
        return transactionIdName;
    }

    public void setTransactionIdName(String transactionIdName) {
        this.transactionIdName = transactionIdName;
    }

    public boolean isAcceptExternalTransactionId() {
        return acceptExternalTransactionId;
    }

    public void setAcceptExternalTransactionId(boolean acceptExternalTransactionId) {
        this.acceptExternalTransactionId = acceptExternalTransactionId;
    }
}