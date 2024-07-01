package org.lib.config;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.lib.advice.AuditLogAspect;
import org.lib.advice.annotation.AuditLog;
import org.lib.appender.CustomConsoleAppender;
import org.lib.appender.CustomFileAppender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Class for autoconfiguring Spring Starter
 * <ul>
 *     <li>To turn on console logging set auditlog.console.enabled=true</li>
 *     <li>To turn on file logging set auditlog.file.enabled=true</li>
 *     <li>To specify file for logs set auditlog.file.path=path...</li>
 * </ul>
 * Configure these variables in application.properties file.
 * @author Matushkin Anton
 */
@Configuration
@ConditionalOnClass({AuditLog.class})
@EnableAspectJAutoProxy
@EnableConfigurationProperties(AuditLibProperties.class)
public class AuditLibSpringBootStarterAutoConfiguration {

    @Value("${auditlog.console.enabled}")
    private boolean consoleLoggingEnabled;

    @Value("${auditlog.file.enabled}")
    private boolean fileLoggingEnabled;

    @Value("${auditlog.file.path}")
    private String logFilePath;

    @Bean
    @ConditionalOnMissingBean
    public AuditLogAspect auditLogAdvice() {
        configureLoggers();
        return new AuditLogAspect();
    }

    /**
     * Method configuring loggers. If auditlog.console.enabled is true console appender is ON,
     * if false - appender is OFF. Similarly, with the other appender.
     */
    private void configureLoggers() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = context.getConfiguration();
        LoggerConfig rootLoggerConfig = config.getRootLogger();

        rootLoggerConfig.getAppenders().forEach((name, appender) -> rootLoggerConfig.removeAppender(name));

        if (consoleLoggingEnabled) {
            Appender consoleAppender = CustomConsoleAppender.createAppender(
                    "ConsoleAppender",
                    null,
                    PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n").build(),
                    true,
                    null);
            consoleAppender.start();
            rootLoggerConfig.addAppender(consoleAppender, Level.ALL, null);
        }

        if (fileLoggingEnabled) {
            CustomFileAppender fileAppender = CustomFileAppender.createAppender(
                    "FileAppender",
                    null,
                    PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n").build(),
                    true,
                    null);
            fileAppender.setPath(logFilePath);
            fileAppender.start();
            rootLoggerConfig.addAppender(fileAppender, Level.ALL, null);
        }

        context.updateLoggers();
    }

}
