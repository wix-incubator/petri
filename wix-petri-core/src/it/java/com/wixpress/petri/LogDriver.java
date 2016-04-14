package com.wixpress.petri;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.rolling.RollingFileAppender;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 8/6/14
* Time: 2:34 PM
* To change this template use File | Settings | File Templates.
*/
public class LogDriver {

    private final File logFile;

    public LogDriver() {
        this.logFile = new File(getLogFileFullName());
    }

    private String getLogFileFullName() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        RollingFileAppender appender = (RollingFileAppender) context.getLogger("experimentsLog").getAppender("EXPERIMENTS_APPENDER");

        return appender.getFile();
    }

    public String lastLongEntry() throws IOException {
        List<String> logLines = logEntries();
        return logLines.get(logLines.size() - 1);
    }

    public List<String> logEntries() throws IOException {
        return FileUtils.readLines(logFile);
    }

    /**
     * Call this @Before each test
     */
    public void cleanup() throws FileNotFoundException {
        try (PrintWriter printWriter = new PrintWriter(logFile)) {
            printWriter.print("");
        }
    }
}
