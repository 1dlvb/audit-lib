package com.onedlvb.advice;

import com.onedlvb.advice.annotation.AuditLog;
import com.onedlvb.config.AuditLibProperties;
import com.onedlvb.kafka.AuditProducer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogAspectTests {

    @Mock
    private AuditProducer producer;

    @Mock
    private AuditLibProperties properties;

    @InjectMocks
    private AuditLogAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(properties.isKafkaLogEnabled()).thenReturn(true);
    }

    @Test
    void testIsIntegerMethodWithTwoArgsGeneratesProperLogAndSendsMessageToKafka() throws Throwable {
        when(signature.getName()).thenReturn("addIntegers");
        when(joinPoint.getArgs()).thenReturn(new Object[]{5, 10});
        when(joinPoint.proceed()).thenReturn("returnValue");

        AuditLog auditLog = mock(AuditLog.class);
        when(auditLog.logLevel()).thenReturn(LogLevel.INFO);

        ReflectionTestUtils.setField(aspect, "applicationName", "test-application");
        ReflectionTestUtils.setField(aspect, "defaultTopic", "default-topic");

        when(joinPoint.proceed()).thenReturn(15);

        assertEquals(15, aspect.logMethodInfo(joinPoint, auditLog));

        ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer, times(1)).sendMessage(eq("default-topic"), messageCaptor.capture());

        Map<String, String> message = messageCaptor.getValue();
        assertEquals("addIntegers", message.get("methodName"));
        assertEquals("Args: [5, 10]", message.get("methodArgs"));
        assertEquals("15", message.get("returnValue"));
    }


    @Test
    void testMethodInvocationWhenExceptionThrownAndCorrectExceptionLoggedAndSendsMessageToKafka() throws Throwable {
        when(signature.getName()).thenReturn("testMethodThatThrowsException");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Test exception"));

        AuditLog auditLog = mock(AuditLog.class);
        when(auditLog.logLevel()).thenReturn(LogLevel.INFO);

        ReflectionTestUtils.setField(aspect, "applicationName", "test-application");
        ReflectionTestUtils.setField(aspect, "defaultTopic", "default-topic");

        assertThrows(RuntimeException.class, () -> aspect.logMethodInfo(joinPoint, auditLog));

        ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer).sendMessage(anyString(), messageCaptor.capture());

        Map<String, String> message = messageCaptor.getValue();
        assertEquals("testMethodThatThrowsException", message.get("methodName"));
        assertEquals("Args: " + Arrays.toString(new Object[]{"arg1", "arg2"}), message.get("methodArgs"));
        assertEquals("java.lang.RuntimeException: Test exception", message.get("exception"));
    }

    @Test
    void testIsVoidMethodWithNoArgsGeneratesProperLogAndSendsMessageToKafka() throws Throwable {
        when(signature.getName()).thenReturn("voidMethodWithNoParams");

        AuditLog auditLog = mock(AuditLog.class);
        when(auditLog.logLevel()).thenReturn(LogLevel.INFO);

        ReflectionTestUtils.setField(aspect, "applicationName", "test-application");
        ReflectionTestUtils.setField(aspect, "defaultTopic", "default-topic");

        assertNull(aspect.logMethodInfo(joinPoint, auditLog));

        ArgumentCaptor<Map<String, String>> messageCaptor = ArgumentCaptor.forClass(Map.class);
        verify(producer, times(1)).sendMessage(eq("default-topic"), messageCaptor.capture());

        Map<String, String> sentMessage = messageCaptor.getValue();

        assertEquals("voidMethodWithNoParams", sentMessage.get("methodName"));
        assertEquals("No args", sentMessage.get("methodArgs"));

    }

}
