package com.onedlvb.config;

import com.onedlvb.advice.AuditLogAspect;
import com.onedlvb.advice.CustomRequestBodyAdvice;
import com.onedlvb.advice.CustomResponseBodyAdvice;
import com.onedlvb.advice.annotation.AuditLog;
import com.onedlvb.advice.annotation.AuditLogHttp;
import com.onedlvb.appender.CustomConsoleAppender;
import com.onedlvb.appender.CustomFileAppender;
import com.onedlvb.interceptor.HttpInterceptor;
import com.onedlvb.kafka.AuditProducer;
import lombok.NonNull;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Class for autoconfiguring Spring Starter
 * <ul>
 *     <li>To turn on console logging set audit-lib-spring-boot-starter..console-enabled=true</li>
 *     <li>To turn on file logging set audit-lib-spring-boot-starter.file-enabled=true</li>
 *     <li>To turn on messaging to kafka set audit-lib-spring-boot-starter.kafka-log-enabled=true</li>
 *     <li>Set up the path for logs: audit-lib-spring-boot-starter.file-path=...</li>
 *     <li>Set up the transactional id: audit-lib-spring-boot-starter.kafka-transactional-id=yourID</li>
 * </ul>
 * Configure these variables in application.properties file.
 * @author Matushkin Anton
 */
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(AuditLibProperties.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@ConditionalOnClass({AuditLog.class, AuditLogHttp.class})
public class AuditLibSpringBootStarterAutoConfiguration implements WebMvcConfigurer {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @NonNull
    private final AuditLibProperties properties;

    public AuditLibSpringBootStarterAutoConfiguration(@NonNull AuditLibProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditLogAspect auditLogAspect() {
        configureLoggers();
        return new AuditLogAspect(auditProducer(), properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomResponseBodyAdvice customResponseBodyAdvice() {
        return new CustomResponseBodyAdvice();
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomRequestBodyAdvice customRequestBodyAdvice() {
        return new CustomRequestBodyAdvice();
    }

    @Bean
    public AuditProducer auditProducer() {
        return new AuditProducer(properties);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HttpInterceptor());
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

        if (properties.isConsoleEnabled()) {
            Appender consoleAppender = CustomConsoleAppender
                    .createAppender(
                            "ConsoleAppender",
                            null,
                            PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n").build(),
                            true,
                            null);
            consoleAppender.start();
            rootLoggerConfig.addAppender(consoleAppender, Level.ALL, null);
        }

        if (properties.isFileEnabled()) {
            CustomFileAppender fileAppender = CustomFileAppender.createAppender(
                    "FileAppender",
                    null,
                    PatternLayout.newBuilder().withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n").build(),
                    true,
                    null);
            fileAppender.setPath(properties.getFilePath());
            fileAppender.start();
            rootLoggerConfig.addAppender(fileAppender, Level.ALL, null);
        }

        context.updateLoggers();
    }

}
