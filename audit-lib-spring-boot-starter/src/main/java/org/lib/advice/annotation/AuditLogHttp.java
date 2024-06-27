package org.lib.advice.annotation;

import org.lib.advice.LogLevel;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for logging http requests and responses. Should be used with controller methods.
 * @author Matushkin Anton
 */
@RestController
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLogHttp {

    /**
     * Parameter configuring {@link LogLevel}. By default log level is DEBUG
     */
    LogLevel logLevel() default LogLevel.DEBUG;

}
