package com.onedlvb.appender;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;


/**
 * Custom appender for logging to a file.
 * @author Matushkin Anton
 */
@Plugin(name = "CustomFileAppender", category = "Core", elementType = "appender", printObject = true)
public class CustomFileAppender extends AbstractAppender {

    private String path;

    protected CustomFileAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    /**
     * Provides output to a file
     * @param event event to output
     */
    @Override
    public void append(LogEvent event) {
        final byte[] bytes = getLayout().toByteArray(event);
        try {
            File logFile = new File(path);
            if (!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
                fos.write(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while log file  creation.");
        }
    }

    /**
     * @param path path for log file.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @param name name of an appender
     * @param filter filters for appender
     * @param layout layout for appender
     * @param ignoreExceptions true to ignore, false to not ingonre
     * @param properties properties for appender
     * @return new {@link CustomFileAppender}
     */
    @PluginFactory
    public static CustomFileAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filters") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
            @PluginElement("Properties") Property[] properties) {
        return new CustomFileAppender(name, filter, layout, ignoreExceptions, properties);
    }

}
