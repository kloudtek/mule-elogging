package org.mule.extension.internal;

//import org.apache.logging.log4j.Level;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.message.MapMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.mule.extension.api.Severity;
import org.mule.runtime.api.message.ItemSequenceInfo;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.util.Base64;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.parameter.CorrelationInfo;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static com.kloudtek.mule.elogging.log4j2.ELJsonLayout.RAWJSON_MARKER;

/**
 * ELogger operations
 */
@SuppressWarnings("unchecked")
public class EloggerOperations {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EloggerOperations.class);
    private HashSet<String> textTypes = new HashSet<>(Arrays.asList("application/json", "application/xml"));

    @Inject
    private TransformationService transformationService;

    @Parameter
    @Optional(defaultValue = "payload")
    private String category;

    @Parameter
    @Optional(defaultValue = "DEBUG")
    private Severity severity;

    /**
     * Log flow state data (payload, attributes, variables) at a given point in time
     */
    public void logState(@Optional(defaultValue = "#[payload]") TypedValue<InputStream> payload,
                         @Optional(defaultValue = "#[output application/json --- attributes]") TypedValue<Object> attributes,
                         CompletionCallback<Void, Void> callback,
                         CorrelationInfo correlationInfo) {
        Logger logger = getLogger(category);
        try {
            if (logger.isEnabled(severity.getLevel())) {
                HashMap<String, Object> data = new HashMap<>();
                addCorrelationInfo(data, correlationInfo);
                addStreamToMap("request.payload", data, payload);
                addObjToMap("request.attributes", data, attributes);
                MapMessage mapMessage = new MapMessage(data);
                logger.log(severity.getLevel(), mapMessage);
            }
        } catch (Throwable e) {
            logger.error("An error occurred while logging payload: " + e.getMessage(), e);
        }
        callback.success(null);
    }

    public void logScope(@Optional(defaultValue = "#[payload]") TypedValue<InputStream> payload,
                         @Optional(defaultValue = "#[output application/json --- attributes]") TypedValue<Object> attributes,
                         Chain operations,
                         CompletionCallback<Object, Object> callback,
                         CorrelationInfo correlationInfo) {
        Logger logger = getLogger(category);
        try {
            if (logger.isEnabled(severity.getLevel())) {
                HashMap<String, Object> data = new HashMap<>();
                addCorrelationInfo(data, correlationInfo);
                addStreamToMap("request.payload", data, payload);
                addObjToMap("request.attributes", data, attributes, true);
                long start = System.currentTimeMillis();
                operations.process(result -> {
                    addDuration(data, start);
                    addObjToMap("response.payload", data, result.getOutput(), result.getMediaType());
                    logger.log(severity.getLevel(), new MapMessage(data));
                    callback.success(result);
                }, (throwable, result) -> {
                    addDuration(data, start);
                });
            } else {
                operations.process(callback::success, (throwable, result) -> callback.error(throwable));
            }
        } catch (Throwable e) {
            logger.error("An error occurred while logging payload: " + e.getMessage(), e);
        }
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
        addObjToMap(key, data, attributes, false);
    }

    private void addObjToMap(String key, HashMap<String, Object> data, TypedValue<Object> attributes, boolean supportRawJson) {
        addObjToMap(key, data, attributes.getValue(), attributes.getDataType(), supportRawJson);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void addObjToMap(String key, HashMap<String, Object> data, Object obj, java.util.Optional<MediaType> mediaTypeOptional) {
        if (mediaTypeOptional.isPresent()) {
            addObjToMap(key, data, obj, DataType.builder().mediaType(mediaTypeOptional.get()).build());
        } else {
            addObjToMap(key, data, obj, DataType.fromObject(obj));
        }
    }


    private void addObjToMap(String key, HashMap<String, Object> data, Object obj, DataType dataType) {
        addObjToMap(key, data, obj, dataType, false);
    }

    private void addObjToMap(String key, HashMap<String, Object> data, Object obj, DataType dataType, boolean supportRawJson) {
        String attrStr = null;
        if (obj != null) {
            if (obj instanceof String) {
                attrStr = obj.toString();
            } else if (obj instanceof InputStream) {
                addStreamToMap(key, data, (InputStream) obj, dataType, supportRawJson);
                return;
            } else {
                try {
                    attrStr = transformationService.transform(obj, dataType, DataType.JSON_STRING).toString();
                } catch (Exception e) {
                    logger.info(e.getMessage(), e);
                }
                if (attrStr == null) {
                    attrStr = obj.toString();
                }
            }
        }
        data.put(key, attrStr);
        if (supportRawJson && dataType.equals(DataType.JSON_STRING)) {
            data.put(key+RAWJSON_MARKER,"true");
        }
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
        if (supportRawJson && ( dataType.isCompatibleWith(DataType.JSON_STRING) || dataType.getMediaType().toRfcString().startsWith("application/json") ) ) {
            data.put(key+RAWJSON_MARKER,"true");
        }
    }

    private boolean isText(MediaType mediaType) {
        String primaryType = mediaType.getPrimaryType().toLowerCase();
        String type = MediaType.create(primaryType, mediaType.getSubType()).toRfcString();
        return type.startsWith("text/") || textTypes.contains(type);
    }

    private Logger getLogger(@Optional(defaultValue = "") String category) {
        return LogManager.getLogger(category);
    }

}
