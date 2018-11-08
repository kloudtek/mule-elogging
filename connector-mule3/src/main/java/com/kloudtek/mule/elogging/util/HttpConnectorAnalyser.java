package com.kloudtek.mule.elogging.util;

import org.mule.api.processor.MessageProcessor;
import org.mule.module.http.internal.request.DefaultHttpRequester;
import org.mule.module.http.internal.request.DefaultHttpRequesterConfig;

import java.util.HashMap;
import java.util.Map;

public class HttpConnectorAnalyser extends ConnectorAnalyser<DefaultHttpRequester> {
    @Override
    public boolean supports(MessageProcessor messageProcessor) {
        return messageProcessor instanceof DefaultHttpRequester;
    }

    @Override
    public Map<String, String> doAnalyse(DefaultHttpRequester messageProcessor) {
        HashMap<String, String> info = new HashMap<>();
        info.put("host",messageProcessor.getHost());
        info.put("method",messageProcessor.getMethod());
        info.put("port",messageProcessor.getPort());
        String path = messageProcessor.getPath();
        DefaultHttpRequesterConfig config = messageProcessor.getConfig();
        if( config != null ) {
            if( config.getBasePath() != null ) {
                String basePath = config.getBasePath();
                if( basePath.endsWith("/") || path.startsWith("/") ) {
                    path = basePath + path;
                } else {
                    path = basePath + "/" + path;
                }
            }
            info.put("scheme",config.getScheme());
        }
        info.put("path",path);
        return info;
    }
}
