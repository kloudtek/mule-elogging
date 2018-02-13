package com.kloudtek.mule.elogging.log4j2;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.Test;

class ELJsonLayoutTest {
    private Logger logger = LogManager.getLogger(ELJsonLayoutTest.class.getName());
    private static final Marker SQL_MARKER = MarkerManager.getMarker("SQL");
    private static final Marker UPDATE_MARKER = MarkerManager.getMarker("SQL_UPDATE").setParents(SQL_MARKER);

    @Test
    public void testLog() {
        Logger logger = LogManager.getLogger("HelloWorld");
        ThreadContext.put("application","baa");
        ThreadContext.push("foo");
        ThreadContext.push("ba\nfdfd\r\n\r");
        logger.info(UPDATE_MARKER,"testing\n", new Exception("DSsd"));
    }
}