package org.lib.advice;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lib.TestConfig;
import org.lib.advice.annotation.AuditLog;
import org.lib.appender.TestAppender;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;


@SpringBootTest
@ContextConfiguration(classes = {
        TestConfig.class,
})
@ExtendWith(MockitoExtension.class)
public class AuditLogAspectTests {

    @Autowired
    private TestService testService;

    @BeforeEach
    public void setUp() {
        TestAppender.getLogEvents().clear();
    }

    @Test
    public void testAuditLogAnnotationWithInfoLevelOnPerformActionMethodWithGivenParameter() {
        testService.performAction("expectedParam1");
        Assertions.assertTrue(TestAppender.getLogEvents().stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "Method name: performAction, Args: [expectedParam1], Return value: expectedParam1")));
        Assertions.assertTrue(TestAppender.getLogEvents().stream()
                .anyMatch(event -> event.getLevel().equals(Level.INFO)));
    }
    @Test
    public void testAuditLogAnnotationWithDebugLevelOnCalculateAvgMethodWithGivenTwoParameters() {
        testService.calculateAvg(1, 2);
        Assertions.assertTrue(TestAppender.getLogEvents().stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "Method name: calculateAvg, Args: [1, 2], Return value: 0.5")));
        Assertions.assertTrue(TestAppender.getLogEvents().stream()
                .anyMatch(event -> event.getLevel().equals(Level.DEBUG)));
    }
    @Test
    public void testAuditLogAnnotationWithDebugLevelOnCalculateAvgMethodWithGivenTwoParametersProvidingToIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> testService.calculateAvg(1, 0));
        Assertions.assertTrue(TestAppender.getLogEvents().stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "Method name: calculateAvg, Args: [1, 0]," +
                                " Exception occurred: java.lang.IllegalArgumentException: Possible zero division!")));
        Assertions.assertTrue(TestAppender.getLogEvents().stream()
                .anyMatch(event -> event.getLevel().equals(Level.DEBUG)));
    }

    @Test
    public void testAuditLogAnnotationWithInfoLevelOnPrintHelloMethodWithNoParameters() {
        testService.printHello();
        Assertions.assertTrue(TestAppender.getLogEvents().stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "Method name: printHello, No args, Return type: void")));
        Assertions.assertTrue(TestAppender.getLogEvents().stream()
                .anyMatch(event -> event.getLevel().equals(Level.INFO)));
    }

    @Test
    public void testAuditLogAnnotationWithWarnLevelOnThrowIllegalArgumentExceptionMethodWithNoParameters() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> testService.throwIllegalArgumentException());

        Assertions.assertTrue(TestAppender.getLogEvents().stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "Method name: throwIllegalArgumentException, No args," +
                                " Exception occurred: java.lang.IllegalArgumentException")));
        Assertions.assertTrue(TestAppender.getLogEvents().stream()
                .anyMatch(event -> event.getLevel().equals(Level.WARN)));
    }

    @Service
    public static class TestService {

        @AuditLog(logLevel = LogLevel.INFO)
        public String performAction(String action) {
            return action;
        }

        @AuditLog
        public Double calculateAvg(Integer a, Integer b) {
            if (b == 0) {
                throw new IllegalArgumentException("Possible zero division!");
            }
            return (double) a / b;
        }

        @AuditLog(logLevel = LogLevel.INFO)
        public void printHello() {
            System.out.println("Hello");
        }

        @AuditLog(logLevel = LogLevel.WARN)
        public void throwIllegalArgumentException() {
            throw new IllegalArgumentException();
        }

    }

}

