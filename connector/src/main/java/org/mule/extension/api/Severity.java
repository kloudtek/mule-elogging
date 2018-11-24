package org.mule.extension.api;

import org.apache.logging.log4j.Level;

public enum Severity {
    OFF(Level.OFF), FATAL(Level.FATAL), ERROR( Level.ERROR), WARN(Level.WARN), INFO(Level.INFO), DEBUG(Level.DEBUG),
    TRACE(Level.TRACE);
    private Level level;

    Severity(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }
}
