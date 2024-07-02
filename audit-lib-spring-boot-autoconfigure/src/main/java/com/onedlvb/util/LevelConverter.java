package com.onedlvb.util;

import com.onedlvb.advice.LogLevel;
import org.apache.logging.log4j.Level;

/**
 * Utility class for converting level of logging from {@link LogLevel} to {@link Level}
 * @author Matushkin Anton
 */
public final class LevelConverter {

    private LevelConverter() {}

    public static Level convertLevel(LogLevel logLevel) {
        return switch (logLevel) {
            case DEBUG -> Level.DEBUG;
            case ERROR -> Level.ERROR;
            case INFO -> Level.INFO;
            case WARN -> Level.WARN;
            case FATAL -> Level.FATAL;
            case OFF -> Level.OFF;
            case TRACE -> Level.TRACE;
        };
    }

}
