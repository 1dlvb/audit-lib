package org.lib.advice;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging basic information of a method.
 * Logs method name, args, returned value (if there is one) and exception (if there is one).
 * @author Matushkin Anton
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger LOGGER = LogManager.getLogger(AuditLogAspect.class);

    @Pointcut("execution(@AuditLog * *(..)) && @annotation(auditLog)")
    private void auditLogPointcut(AuditLog auditLog) {
    }

    /**
     * Performs logging of method execution details using the logging level specified in the AuditLog annotation.
     *
     * @param joinPoint provides the context of the method execution
     * @param auditLog  the annotation containing logging settings for the method
     * @return          the object returned by the target method, or null if the method returns void
     * @throws Throwable any exceptions thrown by the target method during execution
     */
    @Around(value = "auditLogPointcut(auditLog)", argNames = "joinPoint, auditLog")
    public Object logMethodInfo(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Object[] methodArgs = joinPoint.getArgs();

        String methodArgsLog = methodArgs.length > 0 ? String.format("Args: %s",
                Arrays.toString(methodArgs)) : "No args";

        Level level = getLogLevel(auditLog.logLevel());

        try {
            Object proceed = joinPoint.proceed();
            if (!signature.getReturnType().equals(void.class)) {
                LOGGER.log(level, "Method name: {}, {}, Return value: {}",
                        methodName, methodArgsLog, proceed);
            } else {
                LOGGER.log(level, "Method name: {}, {}, Return type: {}",
                        methodName, methodArgsLog, signature.getReturnType());
            }
            return proceed;
        } catch (Throwable throwable) {
            LOGGER.log(level, "Method name: {}, {}, Exception occurred: {}",
                    methodName, methodArgsLog, throwable.getMessage());
            throw throwable;
        }
    }

    /**
     * @param logLevel level of logging, passed from annotation
     * @return log level accepted by the logger
     */
    private Level getLogLevel(LogLevel logLevel) {
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
