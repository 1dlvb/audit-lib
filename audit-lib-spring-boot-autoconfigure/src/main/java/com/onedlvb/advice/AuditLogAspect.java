package com.onedlvb.advice;

import com.onedlvb.advice.annotation.AuditLog;
import com.onedlvb.config.AuditLibProperties;
import com.onedlvb.kafka.AuditProducer;
import com.onedlvb.util.LevelConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aspect for logging basic information of a method.
 * Logs method name, args, returned value (if there is one) and exception (if there is one).
 * @author Matushkin Anton
 */
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private static final Logger LOGGER = LogManager.getLogger(AuditLogAspect.class);

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.kafka.template.default-topic}")
    private String defaultTopic;

    @NonNull
    private final AuditProducer producer;

    @NonNull
    private final AuditLibProperties properties;

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
        String methodArgsLog = formatMethodArgs(methodArgs);

        Map<String, String> message = createMessageForKafka(methodName, methodArgsLog);

        Level level = LevelConverter.convertLevel(auditLog.logLevel());
        try {
            Object returnValue = joinPoint.proceed();
            logMethodExecution(level, methodName, methodArgsLog, returnValue);
            addReturnValueToMessage(message, returnValue);
            return returnValue;
        } catch (Throwable throwable) {
            logMethodWithException(level, methodName, methodArgsLog, throwable);
            addExceptionToMessage(throwable, message);
            throw throwable;
        } finally {
            sendKafkaMessage(message);
        }
    }

    private void sendKafkaMessage(Map<String, String> message) {
        if (properties.isKafkaLogEnabled()) {
            producer.sendMessage(defaultTopic, message);
        }
    }

    private void addReturnValueToMessage(Map<String, String> message, Object returnValue) {
        if (returnValue != null) {
            message.put("returnValue", returnValue.toString());
        }
    }

    private static void addExceptionToMessage(Throwable throwable, Map<String, String> message) {
        message.put("exception", String.valueOf(throwable));
    }

    private void logMethodExecution(Level level, String methodName, String methodArgsLog, Object returnValue) {
        if (returnValue != null) {
            LOGGER.log(level, "Method name: {}, {}, Return value: {}", methodName, methodArgsLog, returnValue);
        } else {
            LOGGER.log(level, "Method name: {}, {}, Return type: void", methodName, methodArgsLog);
        }
    }

    private void logMethodWithException(Level level, String methodName, String methodArgsLog, Throwable throwable) {
        LOGGER.log(level, "Method name: {}, {}, Exception occurred: {}", methodName, methodArgsLog, throwable);
    }

    private static String formatMethodArgs(Object[] methodArgs) {
        return methodArgs != null && methodArgs.length > 0 ? String.format("Args: %s",
                Arrays.toString(methodArgs)) : "No args";
    }

    private Map<String, String> createMessageForKafka(String methodName, String methodArgs) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("serviceName", applicationName);
        message.put("methodName", methodName);
        message.put("methodArgs", methodArgs);
        return message;
    }

}
