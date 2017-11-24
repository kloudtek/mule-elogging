package com.kloudtek.mule.ulogging.log4j2;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ULJsonLayoutTest {
    private Logger logger = LogManager.getLogger(ULJsonLayoutTest.class.getName());
    private static final Marker SQL_MARKER = MarkerManager.getMarker("SQL");
    private static final Marker UPDATE_MARKER = MarkerManager.getMarker("SQL_UPDATE").setParents(SQL_MARKER);

    @Test
    public void testLog() {
        Logger logger = LogManager.getLogger("HelloWorld");
        ThreadContext.put("application","baa");
        logger.info(UPDATE_MARKER,"testing", new Exception("DSsd"));
    }
}