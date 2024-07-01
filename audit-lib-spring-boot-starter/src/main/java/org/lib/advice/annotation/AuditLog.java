
package org.lib.advice.annotation;

import org.lib.advice.LogLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for logging basic method info.
 * @author Matushkin Anton
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * Parameter configuring {@link LogLevel}. By default log level is DEBUG
     */
    LogLevel logLevel() default LogLevel.DEBUG;

}
