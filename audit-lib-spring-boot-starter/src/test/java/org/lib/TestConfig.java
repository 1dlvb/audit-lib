package org.lib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.lib.appender.TestAppender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for tests
 * @author Matushkin Anton
 */
@Configuration
@ComponentScan(basePackages = "org.lib")
public class TestConfig {
    @Bean
    public TestAppender testAppender() {
        TestAppender appender = new TestAppender("TestAppender");
        appender.start();
        return appender;
    }

    @Bean
    public LoggerContext loggerContext(TestAppender appender) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.getConfiguration().getRootLogger().addAppender(appender, null, null);
        context.updateLoggers();
        return context;
    }
}