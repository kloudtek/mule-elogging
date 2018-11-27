package org.mule.extension.internal;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.mule.extension.api.Severity;
import org.mule.runtime.api.el.BindingContext;
import org.mule.runtime.api.message.ItemSequenceInfo;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.el.ExpressionManager;
import org.mule.runtime.core.api.util.Base64;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.parameter.ParameterResolver;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * ELogger operations
 */
@SuppressWarnings("unchecked")
public class EloggerOperations {
    public static final String RAWJSON_MARKER = "_$_rawjson_$_";
    private HashSet<String> textTypes = new HashSet<>(Arrays.asList("application/json", "application/xml"));

    @Inject
    private TransformationService transformationService;
    @Inject
    private ExpressionManager expressionManager;

    @Parameter
    @Optional(defaultValue = "elogger")
    private String category;

    /**
     * Log flow state data (payload, attributes, variables) at a given point in time
     */
    public void logState(@Optional(defaultValue = "#[payload]") ParameterResolver<TypedValue<InputStream>> payload,
                         @Optional(defaultValue = "#[output application/json --- attributes]") ParameterResolver<TypedValue<Object>> attributes,
                         @Optional(defaultValue = "#[output application/json --- vars]") ParameterResolver<TypedValue<Object>> variables,
                         @Optional(defaultValue = "DEBUG") ParameterResolver<Severity> severity,
                         CompletionCallback<Void, Void> callback,
                         CorrelationInfo correlationInfo) {
        Logger logger = getLogger(category);
        try {
            Level logLevel = severity.resolve().getLevel();
            if (logger.isEnabled(logLevel)) {
                HashMap<String, Object> data = new HashMap<>();
                addCorrelationInfo(data, correlationInfo);
                addStreamToMap("request.payload", data, payload.resolve());
                addObjToMap("request.attributes", data, attributes.resolve());
                addObjToMap("request.variables", data, variables.resolve());
                MapMessage mapMessage = new MapMessage(data);
                logger.log(logLevel, mapMessage);
            }
        } catch (Throwable e) {
            logger.error("An error occurred while logging payload: " + e.getMessage(), e);
        }
        callback.success(null);
    }

    /**
     * Scope that logs both incoming and outgoing state
     *
     * @param payload                         Incoming payload
     * @param attributes                      Incoming attributes
     * @param convertResponseAttributesToJson Indicates if response attributes should be converted to raw json
     * @param operations                      Operations
     * @param callback                        Callback
     * @param correlationInfo                 correlation info
     */
    public void logRequestResponseState(@Optional(defaultValue = "#[payload]") ParameterResolver<TypedValue<InputStream>> payload,
                                        @Optional(defaultValue = "#[output application/json --- attributes]") ParameterResolver<TypedValue<Object>> attributes,
                                        @Optional(defaultValue = "#[output application/json --- vars]") ParameterResolver<TypedValue<Object>> variables,
                                        @Optional(defaultValue = "true") boolean convertResponseAttributesToJson,
                                        @Optional(defaultValue = "DEBUG") ParameterResolver<Severity> severity,
                                        @Optional(defaultValue = "DEBUG") ParameterResolver<Severity> severityOnErrorResolver,
                                        Chain operations,
                                        CompletionCallback<Object, Object> callback,
                                        CorrelationInfo correlationInfo) {
        final Logger logger = getLogger(category);
        Level logLevel = severity.resolve().getLevel();
        if (logger.isEnabled(logLevel)) {
            final HashMap<String, Object> data = new HashMap<>();
            try {
                addCorrelationInfo(data, correlationInfo);
                addStreamToMap("request.payload", data, payload.resolve());
                addObjToMap("request.variables", data, variables.resolve());
                addObjToMap("request.attributes", data, attributes.resolve());
            } catch (Exception e) {
                logger.warn("Failed to generate log state: " + e.getMessage(), e);
            }
            long start = System.currentTimeMillis();
            operations.process(result -> {
                try {
                    addResponseState(data, start, result, convertResponseAttributesToJson);
                    logger.log(logLevel, new MapMessage(data));
                } catch (Exception e) {
                    logger.warn("Failed to generate log state: " + e.getMessage(), e);
                }
                callback.success(result);
            }, (throwable, result) -> {
                try {
                    addResponseState(data, start, result, convertResponseAttributesToJson);
                    data.put("throwable.message",throwable.getMessage());
                    data.put("throwable.stacktrace",toString(throwable));
                    logger.log(severityOnErrorResolver.resolve().getLevel(), new MapMessage(data));
                } catch (Exception e) {
                    logger.warn("Failed to generate log state: " + e.getMessage(), e);
                }
                callback.error(throwable);
            });
        } else {
            operations.process(callback::success, (throwable, result) -> callback.error(throwable));
        }
    }

    private void addResponseState(HashMap<String, Object> data, long start, Result result, boolean convertResponseAttributesToJson) {
        addDuration(data, start);
        addObjToMap("response.payload", data, result.getOutput(), result.getMediaType(), false, false);
        addObjToMap("response.attributes", data, result.getAttributes().orElseGet(null), result.getAttributesMediaType(), true, convertResponseAttributesToJson);
    }

    private void addCorrelationInfo(HashMap<String, Object> data, CorrelationInfo correlationInfo) {
        data.put("correlation.id", correlationInfo.getCorrelationId());
        data.put("correlation.eventId", correlationInfo.getEventId());
        if (correlationInfo.getItemSequenceInfo().isPresent()) {
            ItemSequenceInfo itemSequenceInfo = correlationInfo.getItemSequenceInfo().get();
            data.put("correlation.sequence.position", itemSequenceInfo.getPosition());
            if (itemSequenceInfo.getSequenceSize().isPresent()) {
                data.put("correlation.sequence.size", Integer.toString(itemSequenceInfo.getSequenceSize().getAsInt()));
            }
        }
    }

    private void addDuration(HashMap<String, Object> data, long start) {
        data.put("duration", System.currentTimeMillis() - start);
    }

    private void addObjToMap(String key, HashMap<String, Object> data, TypedValue<Object> attributes) {
        addObjToMap(key, data, attributes.getValue(), attributes.getDataType(), true, true);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void addObjToMap(String key, HashMap<String, Object> data, Object obj,
                             java.util.Optional<MediaType> mediaTypeOptional, boolean supportRawJson, boolean convertResponseAttributesToJson) {
        if (mediaTypeOptional.isPresent()) {
            addObjToMap(key, data, obj, DataType.builder().mediaType(mediaTypeOptional.get()).build(), supportRawJson, convertResponseAttributesToJson);
        } else {
            addObjToMap(key, data, obj, DataType.fromObject(obj), supportRawJson, convertResponseAttributesToJson);
        }
    }

    private void addObjToMap(String key, HashMap<String, Object> data, Object obj, DataType dataType) {
        addObjToMap(key, data, obj, dataType, false, true);
    }

    private void addObjToMap(String key, HashMap<String, Object> data, Object obj, DataType dataType, boolean supportRawJson, boolean convertResponseAttributesToJson) {
        String attrStr = null;
        if (obj != null) {
            if (convertResponseAttributesToJson && !(obj instanceof Exception)) {
                try {
                    TypedValue<?> converted = expressionManager.evaluate("output application/json --- data", BindingContext.builder()
                            .addBinding("data", new TypedValue(obj, dataType))
                            .build());
                    obj = converted.getValue();
                    dataType = converted.getDataType();
                } catch (Throwable e) {
                    // failed to convert
                }
            }
            if (obj instanceof String) {
                attrStr = obj.toString();
            } else if (obj instanceof Throwable) {
                try {
                    ObjectComposer<JSONComposer<String>> c = JSON.std.composeString().startObject();
                    c.put("message", ((Throwable) obj).getMessage());
                    c.put("stacktrace", toString((Throwable) obj));
                    attrStr = c.end().finish();
                } catch (IOException e) {
                    attrStr = toString((Throwable) obj);
                }
            } else if (obj instanceof InputStream) {
                addStreamToMap(key, data, (InputStream) obj, dataType, supportRawJson);
                return;
            } else {
                try {
                    attrStr = transformationService.transform(obj, dataType, DataType.JSON_STRING).toString();
                } catch (Throwable e) {
                    // failed to convert
                }
                if (attrStr == null) {
                    attrStr = obj.toString();
                }
            }
        }
        data.put(key, attrStr);
        addRawJsonTag(key, data, dataType, supportRawJson);
    }

    private void addStreamToMap(String prefix, HashMap<String, Object> data, TypedValue<InputStream> payload) {
        addStreamToMap(prefix, data, payload.getValue(), payload.getDataType());
    }

    private void addStreamToMap(String prefix, HashMap<String, Object> data, InputStream stream, DataType dataType) {
        addStreamToMap(prefix, data, stream, dataType, false);
    }

    private void addStreamToMap(String prefix, HashMap<String, Object> data, InputStream stream, DataType dataType, boolean supportRawJson) {
        String payloadValue;
        String payloadType = dataType.getMediaType().toRfcString();
        byte[] payloadValueByteArray = IOUtils.toByteArray(stream);
        if (isText(dataType.getMediaType())) {
            java.util.Optional<Charset> charsetOpt = dataType.getMediaType().getCharset();
            Charset charset = charsetOpt.orElseGet(() -> Charset.forName("UTF-8"));
            payloadValue = new String(payloadValueByteArray, charset);
        } else {
            try {
                payloadValue = Base64.encodeBytes(payloadValueByteArray);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String key = prefix + ".content";
        data.put(key, payloadValue);
        data.put(prefix + ".media-type", payloadType);
        addRawJsonTag(key, data, dataType, supportRawJson);
    }

    private boolean isText(MediaType mediaType) {
        String primaryType = mediaType.getPrimaryType().toLowerCase();
        String type = MediaType.create(primaryType, mediaType.getSubType()).toRfcString();
        return type.startsWith("text/") || textTypes.contains(type);
    }

    private Logger getLogger(@Optional(defaultValue = "") String category) {
        return LogManager.getLogger(category);
    }

    private void addRawJsonTag(String key, HashMap<String, Object> data, DataType dataType, boolean supportRawJson) {
        if (supportRawJson && ((dataType.isCompatibleWith(DataType.JSON_STRING) || dataType.getMediaType().toRfcString().startsWith("application/json")))) {
            data.put(key + RAWJSON_MARKER, "true");
        }
    }

    private static String toString(Throwable e) {
        StringWriter buf = new StringWriter();
        try (PrintWriter w = new PrintWriter(buf)) {
            e.printStackTrace(w);
        }
        return buf.toString();
    }
}
