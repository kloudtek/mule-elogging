package com.kloudtek.mule.ulogging.log4j2;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
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
            json.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(event.getTimeMillis())));
            return json.end().finish() + "\n";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
