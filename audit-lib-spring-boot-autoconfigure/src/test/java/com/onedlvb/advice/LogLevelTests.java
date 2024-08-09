package com.onedlvb.advice;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LogLevelTests {

    @Test
    void testLogLevelEnumValues() {
        Set<LogLevel> expectedLevels = EnumSet.of(
                LogLevel.DEBUG,
                LogLevel.ERROR,
                LogLevel.INFO,
                LogLevel.WARN,
                LogLevel.FATAL,
                LogLevel.OFF,
                LogLevel.TRACE
        );

        Set<LogLevel> actualRoles = EnumSet.allOf(LogLevel.class);

        assertEquals(expectedLevels, actualRoles);
    }

}