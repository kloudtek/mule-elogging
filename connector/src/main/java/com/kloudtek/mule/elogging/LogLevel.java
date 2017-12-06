package com.kloudtek.mule.elogging;


import org.apache.logging.log4j.Level;

public enum LogLevel {
    FATAL(Level.FATAL), ERROR(Level.ERROR), WARN(Level.WARN), INFO(Level.INFO), DEBUG(Level.DEBUG), TRACE(Level.TRACE);
    private Level lvl;

    LogLevel(Level lvl) {
        this.lvl = lvl;
    }

    public Level getLvl() {
        return lvl;
    }
}
