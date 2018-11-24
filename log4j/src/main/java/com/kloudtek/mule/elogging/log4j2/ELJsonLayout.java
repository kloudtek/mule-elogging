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
import org.apache.logging.log4j.message.MapMessage;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Plugin(name = "ELJsonLayout", category = "Core", elementType = "layout", printObject = true)
public class ELJsonLayout extends AbstractStringLayout {
    public static final String RAWJSON_MARKER = "_$_rawjson_$_";
    public static final int RAWJSON_MARK_LEN = RAWJSON_MARKER.length();
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
        ELJsonLayout.prettyPrint = prettyPrint;
        return new ELJsonLayout(charset);
    }

    @SuppressWarnings("unchecked")
    public String toSerializable(LogEvent event) {
        try {
            JSON jbase = JSON.std;
            if (prettyPrint) {
                jbase = jbase.with(JSON.Feature.PRETTY_PRINT_OUTPUT);
            }
            ObjectComposer<JSONComposer<String>> json = jbase
                    .composeString().startObject()
                    .put("loggerName", event.getLoggerName())
                    .put("loggerFqcn", event.getLoggerFqcn())
                    .put("threadName", event.getThreadName())
                    .put("level", event.getLevel().name());
            if (event.getMessage() instanceof MapMessage) {
                Map data = ((MapMessage) event.getMessage()).getData();
                ArrayList<KeyValue> list = new ArrayList<>();
                data.forEach((k, v) -> list.add(new KeyValue(k.toString(), v != null ? v.toString() : null)));
                while (!list.isEmpty()) {
                    KeyValue keyValue = list.remove(0);
                    String key = keyValue.key;
                    String value = keyValue.value;
                    if (!key.endsWith(RAWJSON_MARKER)) {
                        boolean skip = false;
                        if (data.getOrDefault(key + RAWJSON_MARKER, "false").equals("true")) {
                            try {
                                if (key.endsWith(".content")) {
                                    key = key.substring(0, key.length() - 8);
                                    remove(list, key + ".media-type");
                                }
                                Object jsonObj = jbase.anyFrom(value);
                                if (jsonObj != null) {
                                    json.putObject(key, jsonObj);
                                    skip = true;
                                }
                            } catch (Throwable e) {
                                //
                            }
                        }
                        if (!skip) {
                            json.put(key, value);
                        }
                    }
                }
            } else {
                json.put("message", event.getMessage().getFormattedMessage());
            }
            if (event.getThrownProxy() != null) {
                if (getExtendedStackTraceAsStringAvailable) {
                    json.put("stacktrace", event.getThrownProxy().getExtendedStackTraceAsString());
                } else if (event.getThrown() != null) {
                    try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                        event.getThrown().printStackTrace(pw);
                        json.put("stacktrace", sw.toString());
                    }
                }
            }
            if (event.getContextStack() != null) {
                List<String> ndcList = event.getContextStack().asList();
                if (ndcList != null && !ndcList.isEmpty()) {
                    ArrayComposer<ObjectComposer<JSONComposer<String>>> ndcArray = json.startArrayField("logctx.stack");
                    for (String val : ndcList) {
                        ndcArray.add(val);
                    }
                    ndcArray.end();
                }
            }
            if (event.getContextMap() != null && !event.getContextMap().isEmpty()) {
                for (Map.Entry<String, String> entry : event.getContextMap().entrySet()) {
                    json.put("logctx.map." + entry.getKey(), entry.getValue());
                }
            }
            json.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(event.getTimeMillis())));
            return json.end().finish() + "\n";
        } catch (Throwable e) {
            return "ERROR: " + e.getMessage() + " " + Arrays.toString(e.getStackTrace());
        }
    }

    private void remove(ArrayList<KeyValue> list, String key) {
        for (KeyValue keyValue : list) {
            if (keyValue.key.equals(key)) {
                list.remove(keyValue);
                return;
            }
        }
    }

    class KeyValue {
        String key;
        String value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
