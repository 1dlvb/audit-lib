package com.onedlvb.appender;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Custom appender for logging to a list.
 * @author Matushkin Anton
 */
public class TestAppender extends AbstractAppender {

    private static final List<LogEvent> LOG_EVENTS = new CopyOnWriteArrayList<>();

    public TestAppender(String name) {
        super(name, null, null, false, null);
    }

    /**
     * Provides output to a list
     * @param event event to add to list
     */
    @Override
    public void append(LogEvent event) {
        LOG_EVENTS.add(event);
    }

    public static List<LogEvent> getLogEvents() {
        return LOG_EVENTS;
    }

}
