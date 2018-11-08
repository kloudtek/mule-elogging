package com.kloudtek.mule.elogging.log4j2;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.message.MapMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

class ELJsonLayoutTest {
    private static final Marker SQL_MARKER = MarkerManager.getMarker("SQL");
    private static final Marker UPDATE_MARKER = MarkerManager.getMarker("SQL_UPDATE").setParents(SQL_MARKER);

    @Test
    public void testLog() throws Exception {
        String logEntry = captureStdout(() -> {
            LoggerContext ctx = buildJson();
            ctx.getLogger("Hello").info("WORLD");
        } );
        Assertions.assertEquals("{\"loggerName\":\"Hello\",\"loggerFqcn\":\"org.apache.logging.log4j.spi.AbstractLogger\",\"threadName\":\"main\",\"level\":\"INFO\",\"message\":\"WORLD\",\"timestamp\":\"[TIMESTAMP]\"}\n",logEntry);
    }

    @Test
    public void testJonLogMessage() throws Exception {
        String logEntry = captureStdout(() -> {
            LoggerContext ctx = buildJson();
            HashMap<String,String> stuff = new HashMap<>();
            stuff.put("foo","bar");
            stuff.put("a.b.c","2345");
            MapMessage mapMessage = new MapMessage(stuff);
            ctx.getLogger("Hello").log(Level.INFO, mapMessage);
        } );
        Assertions.assertEquals("{\"loggerName\":\"Hello\",\"loggerFqcn\":\"org.apache.logging.log4j.spi.AbstractLogger\",\"threadName\":\"main\",\"level\":\"INFO\",\"a.b.c\":\"2345\",\"foo\":\"bar\",\"timestamp\":\"[TIMESTAMP]\"}\n",logEntry);
    }

    private LoggerContext buildJson() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setPackages(ELJsonLayout.class.getPackage().getName());
        builder.setStatusLevel(Level.ERROR);
        builder.setConfigurationName("BuilderTest");
        builder.add(builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
                .addAttribute("level", Level.DEBUG));
        AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
                ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("ELJsonLayout"));
        appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
                .addAttribute("marker", "FLOW"));
        builder.add(appenderBuilder);
        builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
                .add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));
        builder.add(builder.newRootLogger(Level.ERROR).add(builder.newAppenderRef("Stdout")));
        return Configurator.initialize(builder.build());
    }

    public String captureStdout(Runnable f) throws Exception {
        PrintStream old = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf));
        try {
            f.run();
        } finally {
            System.setOut(old);
        }
        return new String(buf.toByteArray()).replaceAll("\"timestamp\":\".*?\"","\"timestamp\":\"[TIMESTAMP]\"");
    }
}