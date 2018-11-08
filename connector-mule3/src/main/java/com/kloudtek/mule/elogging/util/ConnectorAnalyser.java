package com.kloudtek.mule.elogging.util;

import org.mule.api.processor.MessageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public abstract class ConnectorAnalyser<X extends MessageProcessor> {
    private static List<ConnectorAnalyser> connectorAnalysers;

    public abstract boolean supports(MessageProcessor messageProcessor);
    public abstract Map<String,String> doAnalyse(X messageProcessor);

    @SuppressWarnings("unchecked")
    public static Map<String,String> analyse(MessageProcessor processor ) {
        for (ConnectorAnalyser analyser : getAnalysers()) {
            if( analyser.supports(processor) ) {
                return analyser.doAnalyse(processor);
            }
        }
        return null;
    }

    private static synchronized List<ConnectorAnalyser> getAnalysers() {
        if( connectorAnalysers == null ) {
            ArrayList<ConnectorAnalyser> list = new ArrayList<>();
            try {
                list.add(new HttpConnectorAnalyser());
            } catch (Exception e) {
                //
            }
            ServiceLoader<ConnectorAnalyser> services = ServiceLoader.load(ConnectorAnalyser.class);
            for (ConnectorAnalyser service : services) {
                list.add(service);
            }
            connectorAnalysers = list;
        }
        return connectorAnalysers;
    }
}
