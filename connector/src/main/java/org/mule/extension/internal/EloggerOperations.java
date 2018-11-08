package org.mule.extension.internal;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MapMessage;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.extension.api.annotation.param.Optional;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.HashMap;

/**
 * ELogger operations
 */
public class EloggerOperations {
    private static Logger logger = LogManager.getLogger(EloggerOperations.class);

    @Inject
    private TransformationService transformationService;

    /**
     * Log flow state data at a given point in time
     */
    public void log(@Optional(defaultValue = "#[payload]") TypedValue<InputStream> payload,
                    @Optional(defaultValue = "#[output application/json --- attributes]") TypedValue<Object> attributes) {
        HashMap<String, Object> data = new HashMap<>();
        MapMessage mapMessage = new MapMessage(data);
        logger.log(Level.INFO, mapMessage);
    }
}
