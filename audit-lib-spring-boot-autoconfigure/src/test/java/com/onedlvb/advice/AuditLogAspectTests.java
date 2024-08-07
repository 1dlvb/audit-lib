package com.onedlvb.advice;

import com.onedlvb.advice.annotation.AuditLog;
import com.onedlvb.config.AuditLibProperties;
import com.onedlvb.kafka.AuditProducer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class AuditLogAspectTests {

    @Mock
    private AuditProducer producer;

    @Mock
    private AuditLibProperties auditLibProperties;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Mock
    private Logger logger;

    @Mock
    private AuditLog auditLog;

    @Test
    void testMethodInvocationWhenExceptionThrownAndCorrectExceptionLoggedAndSendsMessageToKafka() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethodThatThrowsException");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        when(auditLog.logLevel()).thenReturn(LogLevel.INFO);
        when(auditLibProperties.isKafkaLogEnabled()).thenReturn(true);

        Exception exception = new RuntimeException("Test exception");

        try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
            Logger mockedLogger = mock(Logger.class);
            mockedLogManager.when(() -> LogManager.getLogger(AuditLogAspect.class)).thenReturn(mockedLogger);

            AuditLogAspect auditLogAspect = new AuditLogAspect(producer, auditLibProperties);
            setField(auditLogAspect, "applicationName", "test-application");
            setField(auditLogAspect, "defaultTopic", "default-topic");

            when(joinPoint.proceed()).thenThrow(exception);

            try {
                auditLogAspect.logMethodInfo(joinPoint, auditLog);
            } catch (Throwable ignored) {
            }

            verify(mockedLogger).log(eq(Level.INFO), eq("Method name: {}, {}, Exception occurred: {}"),
                    eq("testMethodThatThrowsException"),
                    eq("Args: " + Arrays.toString(new Object[]{"arg1", "arg2"})),
                    eq("java.lang.RuntimeException: Test exception"));

            ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
            verify(producer, times(1)).sendMessage(eq("default-topic"), messageCaptor.capture());

            Map<String, String> sentMessage = messageCaptor.getValue();

            assertEquals("testMethodThatThrowsException", sentMessage.get("methodName"));
            assertEquals("Args: " + Arrays.toString(new Object[]{"arg1", "arg2"}), sentMessage.get("methodArgs"));
            assertEquals("java.lang.RuntimeException: Test exception", sentMessage.get("exception"));
        }
    }

    @Test
    void testIsVoidMethodWithNoArgsGeneratesProperLogAndSendsMessageToKafka() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("voidMethodWithNoParams");
        when(auditLog.logLevel()).thenReturn(LogLevel.INFO);
        when(auditLibProperties.isKafkaLogEnabled()).thenReturn(true);

        try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
            mockedLogManager.when(() -> LogManager.getLogger(AuditLogAspect.class)).thenReturn(logger);

            AuditLogAspect auditLogAspect = new AuditLogAspect(producer, auditLibProperties);

            setField(auditLogAspect, "applicationName", "test-application");
            setField(auditLogAspect, "defaultTopic", "default-topic");

            auditLogAspect.logMethodInfo(joinPoint, auditLog);

            verify(logger).log(eq(Level.INFO), eq("Method name: {}, {}, Return type: void"),
                    eq("voidMethodWithNoParams"),
                    eq("No args"));

            ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
            verify(producer, times(1)).sendMessage(eq("default-topic"), messageCaptor.capture());

            Map<String, String> sentMessage = messageCaptor.getValue();

            assertEquals("voidMethodWithNoParams", sentMessage.get("methodName"));
            assertEquals("No args", sentMessage.get("methodArgs"));

        }
    }


    @Test
    void testIsIntegerMethodWithTwoArgsGeneratesProperLogAndSendsMessageToKafka() throws Throwable {
        when(auditLog.logLevel()).thenReturn(LogLevel.valueOf("INFO"));
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("addIntegers");
        when(joinPoint.getArgs()).thenReturn(new Object[]{5, 10});
        when(auditLibProperties.isKafkaLogEnabled()).thenReturn(true);

        try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
            mockedLogManager.when(() -> LogManager.getLogger(AuditLogAspect.class)).thenReturn(logger);

            AuditLogAspect auditLogAspect = new AuditLogAspect(producer, auditLibProperties);

            setField(auditLogAspect, "applicationName", "test-application");
            setField(auditLogAspect, "defaultTopic", "default-topic");

            when(joinPoint.proceed()).thenReturn(15);
            assertEquals(15, auditLogAspect.logMethodInfo(joinPoint, auditLog));

            verify(logger).log(eq(Level.INFO), eq("Method name: {}, {}, Return value: {}"),
                    eq("addIntegers"),
                    eq("Args: " + Arrays.toString(new Object[]{5, 10})),
                    eq(15));

            ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
            verify(producer, times(1)).sendMessage(eq("default-topic"), messageCaptor.capture());

            Map<String, String> sentMessage = messageCaptor.getValue();

            assertEquals("addIntegers", sentMessage.get("methodName"));
            assertEquals("Args: [5, 10]", sentMessage.get("methodArgs"));
            assertEquals("15", sentMessage.get("returnValue"));
        }
    }

}
