package com.kloudtek.mule.elogging.util;

import org.mule.api.processor.MessageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ConnectorAnalyser<X extends MessageProcessor> {
    private static List<ConnectorAnalyser> connectorAnalysers;

    public abstract boolean supports(MessageProcessor messageProcessor);
    public abstract Map<String,String> doAnalyse(X messageProcessor);

    @SuppressWarnings("unchecked")
    public static Map<String,String> analyse(MessageProcessor processor ) {
        if( connectorAnalysers == null ) {
            ArrayList<ConnectorAnalyser> list = new ArrayList<>();
            try {
                list.add(new HttpConnectorAnalyser());
            } catch (Exception e) {
                //
            }
            connectorAnalysers = list;
        }
        for (ConnectorAnalyser analyser : connectorAnalysers) {
            if( analyser.supports(processor) ) {
                return analyser.doAnalyse(processor);
            }
        }
        return null;
    }
}
