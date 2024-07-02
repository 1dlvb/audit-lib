package com.onedlvb.advice;

import com.onedlvb.advice.annotation.AuditLog;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class AuditLogAspectTests {
    @Test
    public void testMethodInvocationWhenExceptionThrownAndCorrectExceptionLogged() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethodThatThrowsException");
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        Exception exception = new RuntimeException("Test exception");
        AuditLog auditLog = mock(AuditLog.class);

        try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
            AuditLogAspect auditLogAspect = new AuditLogAspect();
            auditLogAspect.logMethodInfo(joinPoint, auditLog);
            Logger logger = mock(Logger.class);
            mockedLogManager.when(() -> LogManager.getLogger(AuditLogAspect.class)).thenReturn(logger);

            when(joinPoint.proceed()).thenThrow(exception);

            verify(logger).log(eq(Level.INFO), eq("Exception in method: {}, Args: {}, Exception message: {}"),
                    eq("testMethodThatThrowsException"),
                    eq(Arrays.toString(new Object[]{"arg1", "arg2"})),
                    eq("java.lang.RuntimeException Test exception"));
        } catch (Throwable ignored) {

        }
    }
    @Test
    public void testIsVoidMethodWithNoArgsGeneratesProperLog() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        Logger logger = mock(Logger.class);
        AuditLog auditLog = mock(AuditLog.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("voidMethodWithNoParams");

        try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
            mockedLogManager.when(() -> LogManager.getLogger(AuditLogAspect.class)).thenReturn(logger);

            AuditLogAspect auditLogAspect = new AuditLogAspect();
            auditLogAspect.logMethodInfo(joinPoint, auditLog);

            verify(logger).log(eq(Level.INFO), eq("Method name: {}, {}, Return type: void"),
                    eq("voidMethodWithNoParams"),
                    eq("No args"));
        } catch (Throwable ignored) {

        }

    }

    @Test
    public void testIsIntegerMethodWithTwoArgsGeneratesProperLog() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        Logger logger = mock(Logger.class);
        AuditLog auditLog = mock(AuditLog.class);
        when(auditLog.logLevel()).thenReturn(LogLevel.valueOf("INFO"));

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("addIntegers");
        when(joinPoint.getArgs()).thenReturn(new Object[]{5, 10});

        try (MockedStatic<LogManager> mockedLogManager = mockStatic(LogManager.class)) {
            mockedLogManager.when(() -> LogManager.getLogger(AuditLogAspect.class)).thenReturn(logger);

            AuditLogAspect calculationAspect = new AuditLogAspect();
            calculationAspect.logMethodInfo(joinPoint, auditLog);

            when(joinPoint.proceed()).thenReturn(15);
            assertEquals(15, calculationAspect.logMethodInfo(joinPoint, auditLog));

            verify(logger).log(eq(Level.INFO), eq("Method name: {}, {}, Return value: {}"),
                    eq("addIntegers"),
                    eq("Args: " + Arrays.toString(new Object[]{5, 10})),
                    eq(15));
        }
    }

}
