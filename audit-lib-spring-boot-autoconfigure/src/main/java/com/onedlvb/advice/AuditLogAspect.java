package com.onedlvb.advice;

import com.onedlvb.advice.annotation.AuditLog;
import com.onedlvb.advice.exception.KafkaSendMessageException;
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
     * <p>
     * @param joinPoint provides the context of the method execution
     * @param auditLog  the annotation containing logging settings for the method
     * @return          the object returned by the target method, or null if the method returns void
     * @throws Throwable any exceptions thrown by the target method during execution
     */
    @Around("@annotation(auditLog)")
    public Object logMethodInfo(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String methodArgsLog = formatMethodArgs(joinPoint.getArgs());

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

    /**
     * Performs sending messages to the kafka broker.
     * <p>
     * @param message message that should be sent to the Kafka
     */
    private void sendKafkaMessage(Map<String, String> message) throws KafkaSendMessageException {
        if (properties.isKafkaLogEnabled()) {
            producer.sendMessage(defaultTopic, message);
        }
    }

    /**
     * @param message message, that should be sent to the Kafka
     * @param returnValue return value of the method
     */
    private void addReturnValueToMessage(Map<String, String> message, Object returnValue) {
        if (returnValue != null) {
            message.put("returnValue", returnValue.toString());
        }
    }

    /**
     * @param throwable exception, that should be added to the message
     * @param message message, that should be sent to the Kafka
     */
    private static void addExceptionToMessage(Throwable throwable, Map<String, String> message) {
        message.put("exception", String.valueOf(throwable));
    }

    /**
     * Logs basic info about method annotated with @AuditLog annotation.
     * <p>
     * @param level logging level
     * @param methodName name of the method to log
     * @param methodArgs arguments of the method to log
     * @param returnValue return value of method to log
     */
    private void logMethodExecution(Level level, String methodName, String methodArgs, Object returnValue) {
        if (returnValue != null) {
            LOGGER.log(level, "Method name: {}, {}, Return value: {}", methodName, methodArgs, returnValue);
        } else {
            LOGGER.log(level, "Method name: {}, {}, Return type: void", methodName, methodArgs);
        }
    }

    /**
     * Logs basic info about method and exception annotated with @AuditLog annotation.
     * <p>
     * @param level logging level
     * @param methodName name of the method to log
     * @param methodArgs arguments of the method to log
     * @param throwable method execution exception
     */
    private void logMethodWithException(Level level, String methodName, String methodArgs, Throwable throwable) {
        LOGGER.log(level, "Method name: {}, {}, Exception occurred: {}",
                methodName,
                methodArgs,
                throwable.toString());
    }

    /**
     * Formats args of the method that should be logged.
     * <p>
     * If method has args method returns "Args: ...", otherwise "No args".
     * @param methodArgs arguments to format
     * @return formatted method arguments that should be logged
     */
    private static String formatMethodArgs(Object[] methodArgs) {
        return methodArgs != null && methodArgs.length > 0 ? String.format("Args: %s",
                Arrays.toString(methodArgs)) : "No args";
    }

    /**
     * Creates basic message for kafka.
     * <p>
     * @param methodName method name, that should be added to the kafka
     * @param methodArgs arguments of the method, that should be added to the kafka
     * @return initialized message
     */
    private Map<String, String> createMessageForKafka(String methodName, String methodArgs) {
        Map<String, String> message = new LinkedHashMap<>();
        message.put("serviceName", applicationName);
        message.put("methodName", methodName);
        message.put("methodArgs", methodArgs);
        return message;
    }

}
