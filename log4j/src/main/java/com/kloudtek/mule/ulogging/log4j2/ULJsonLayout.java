package com.kloudtek.mule.ulogging.log4j2;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ArrayComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Plugin(name = "ULJsonLayout", category = "Core", elementType = "layout", printObject = true)
public class ULJsonLayout extends AbstractStringLayout {
    protected ULJsonLayout(Charset charset) {
        super(charset);
    }

    @PluginFactory
    public static ULJsonLayout createLayout(@PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset) {
        return new ULJsonLayout(charset);
    }

    public String toSerializable(LogEvent event) {
        try {
            ObjectComposer<JSONComposer<String>> json = JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT)
                    .composeString().startObject()
                    .put("loggerName", event.getLoggerName())
                    .put("loggerFqcn", event.getLoggerFqcn())
                    .put("threadName", event.getThreadName())
                    .put("level", event.getLevel().name())
                    .put("message", event.getMessage().getFormattedMessage());
            if( event.getThrownProxy() != null ) {
                json.put("stacktrace",event.getThrownProxy().getExtendedStackTraceAsString());
            }
            if( ThreadContext.getImmutableStack() != null ) {
                List<String> ndcList = ThreadContext.getImmutableStack().asList();
                if( ndcList != null && !ndcList.isEmpty() ) {
                    ArrayComposer<ObjectComposer<JSONComposer<String>>> ndcArray = json.startArrayField("tcStack");
                    for (String val : ndcList) {
                        ndcArray.add(val);
                    }
                    ndcArray.end();
                }
            }
            if( ThreadContext.getContext() != null && ! ThreadContext.getContext().isEmpty() ) {
                ObjectComposer<ObjectComposer<JSONComposer<String>>> tcMap = json.startObjectField("tcMap");
                for (Map.Entry<String, String> entry : ThreadContext.getContext().entrySet()) {
                    tcMap.put(entry.getKey(),entry.getValue());
                }
                tcMap.end();
            }
            json.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(event.getTimeMillis())));
            return json.end().finish() + "\n";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
