package com.kloudtek.mule.elogging.log4j2;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ArrayComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Plugin(name = "ELJsonLayout", category = "Core", elementType = "layout", printObject = true)
public class ELJsonLayout extends AbstractStringLayout {
    private static Charset charset;
    private static boolean prettyPrint;
    private static final boolean getExtendedStackTraceAsStringAvailable;

    static {
        Method m;
        try {
            m = ThrowableProxy.class.getDeclaredMethod("getExtendedStackTraceAsString");
        } catch (NoSuchMethodException e) {
            m = null;
        }
        getExtendedStackTraceAsStringAvailable = m != null;
    }

    protected ELJsonLayout(Charset charset) {
        super(charset);
    }

    @PluginFactory
    public static ELJsonLayout createLayout(@PluginAttribute(value = "charset", defaultString = "UTF-8") Charset charset,
                                            @PluginAttribute(value = "prettyPrint", defaultString = "false") boolean prettyPrint) {
        ELJsonLayout.charset = charset;
        ELJsonLayout.prettyPrint = prettyPrint;
        return new ELJsonLayout(charset);
    }

    public String toSerializable(LogEvent event) {
        try {
            JSON jbase = JSON.std;
            if( prettyPrint ) {
                jbase = jbase.with(JSON.Feature.PRETTY_PRINT_OUTPUT);
            }
            ObjectComposer<JSONComposer<String>> json = jbase
                    .composeString().startObject()
                    .put("loggerName", event.getLoggerName())
                    .put("loggerFqcn", event.getLoggerFqcn())
                    .put("threadName", event.getThreadName())
                    .put("level", event.getLevel().name());
            if (event.getMessage() instanceof MuleLogMessage) {
                json.put("message","mule message logged");
                ((MuleLogMessage) event.getMessage()).toJson(json,"mule");
            } else if( event.getMessage() instanceof RequestResponseLogMessage ) {
                ((RequestResponseLogMessage) event.getMessage()).toJson(json);
            } else {
                json.put("message", event.getMessage().getFormattedMessage());
            }
            if (event.getThrownProxy() != null) {
                if( getExtendedStackTraceAsStringAvailable ) {
                    json.put("stacktrace", event.getThrownProxy().getExtendedStackTraceAsString());
                } else if( event.getThrown() != null ) {
                    try( StringWriter sw = new StringWriter() ; PrintWriter pw = new PrintWriter(sw) ) {
                        event.getThrown().printStackTrace(pw);
                        json.put("stacktrace", sw.toString() );
                    }
                }
            }
            if (event.getContextStack() != null) {
                List<String> ndcList = event.getContextStack().asList();
                if (ndcList != null && !ndcList.isEmpty()) {
                    ArrayComposer<ObjectComposer<JSONComposer<String>>> ndcArray = json.startArrayField("tcStack");
                    for (String val : ndcList) {
                        ndcArray.add(val);
                    }
                    ndcArray.end();
                }
            }
            if (event.getContextMap() != null && !event.getContextMap().isEmpty()) {
                ObjectComposer<ObjectComposer<JSONComposer<String>>> tcMap = json.startObjectField("tcMap");
                for (Map.Entry<String, String> entry : event.getContextMap().entrySet()) {
                    tcMap.put(entry.getKey(), entry.getValue());
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
