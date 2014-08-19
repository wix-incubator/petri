package com.wixpress.petri;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

    public LogDriver(String logFileName) {
        this.logFile = new File(logFileName);
    }

    public String lastLongEntry() throws IOException {
        List<String> logLines = logEntries();
        return logLines.get(logLines.size() - 1);
    }

    public List<String> logEntries() throws IOException {
        return FileUtils.readLines(logFile);
    }

    /**
     * Call this @After each test
     * TODO: Maybe make a junit rule for this
     * @throws Exception
     */
    public void cleanup() throws Exception {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        configurator.doConfigure(getClass().getResource("/logback-test.xml"));
    }

}
