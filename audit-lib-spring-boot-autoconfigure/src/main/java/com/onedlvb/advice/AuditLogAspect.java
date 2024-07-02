package com.onedlvb.advice;

import com.onedlvb.advice.annotation.AuditLog;
import com.onedlvb.util.LevelConverter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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

    /**
     * Performs logging of method execution details using the logging level specified in the AuditLog annotation.
     *
     * @param joinPoint provides the context of the method execution
     * @param auditLog  the annotation containing logging settings for the method
     * @return          the object returned by the target method, or null if the method returns void
     * @throws Throwable any exceptions thrown by the target method during execution
     */
    @Around("@annotation(auditLog)")
    public Object logMethodInfo(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] methodArgs = joinPoint.getArgs();

        String methodArgsLog = methodArgs != null && methodArgs.length > 0 ? String.format("Args: %s",
                Arrays.toString(methodArgs)) : "No args";

        Level level = LevelConverter.convertLevel(auditLog.logLevel());

        try {
            Object proceed = joinPoint.proceed();
            if (proceed != null) {
                LOGGER.log(level, "Method name: {}, {}, Return value: {}",
                        methodName, methodArgsLog, proceed);
            } else {
                LOGGER.log(level, "Method name: {}, {}, Return type: void",
                        methodName, methodArgsLog);
            }
            return proceed;
        } catch (Throwable throwable) {
            LOGGER.log(level, "Method name: {}, {}, Exception occurred: {}",
                    methodName, methodArgsLog, throwable.getMessage());
            throw throwable;
        }
    }

}
