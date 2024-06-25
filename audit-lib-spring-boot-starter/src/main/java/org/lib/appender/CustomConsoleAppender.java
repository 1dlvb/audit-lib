package org.lib.appender;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;

/**
 * Custom appender for logging to a console.
 * @author Matushkin Anton
 */
@Plugin(name = "CustomConsoleAppender", category = "Core", elementType = "appender", printObject = true)
public class CustomConsoleAppender extends AbstractAppender {

    protected CustomConsoleAppender(String name, Filter filter, Layout<? extends Serializable> layout,
                                    boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    /**
     * Provides output to a console
     * @param event event to output
     */
    @Override
    public void append(LogEvent event) {
        String message = new String(getLayout().toByteArray(event));
        System.out.print(message);
    }

    /**
     * Factory method
     * @param name name of an appender
     * @param filter filters for appender
     * @param layout layout for appender
     * @param ignoreExceptions true to ignore, false to not ingonre
     * @param properties properties for appender
     * @return new {@link CustomConsoleAppender}
     */
    @PluginFactory
    public static CustomConsoleAppender createAppender(
            @PluginAttribute("name") String name,
           @PluginElement("Filters") Filter filter,
           @PluginElement("Layout") Layout<? extends Serializable> layout,
           @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
           @PluginElement("Properties") Property[] properties) {
        return new CustomConsoleAppender(name, filter, layout, ignoreExceptions, properties);
    }

}
