package org.lib.advice;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lib.service.TestService;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class AuditLogAspectTests {

    private static final List<LogEvent> logEvents = new CopyOnWriteArrayList<>();

    private static class TestAppender extends AbstractAppender {
        protected TestAppender(String name) {
            super(name, null, null, false, null);
        }

        @Override
        public void append(LogEvent event) {
            logEvents.add(event);
        }
    }

    @Autowired
    private TestService testService;
    @BeforeEach
    public void setUp() {
        LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
        Appender appender = new TestAppender("TestAppender");
        appender.start();
        logContext.getRootLogger().addAppender(appender);
    }

    @Test
    public void testAuditLogAnnotationWithInfoLevelOnPerformActionMethodWithGivenParameter() {
        testService.performAction("expectedParam1");
        Assertions.assertTrue(logEvents.stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "Method name: performAction, Args: [expectedParam1], Return value: expectedParam1")));
        Assertions.assertTrue(logEvents.stream()
                .anyMatch(event -> event.getLevel().equals(Level.INFO)));
    }
    @Test
    public void testAuditLogAnnotationWithDebugLevelOnCalculateAvgMethodWithGivenTwoParameters() {
        testService.calculateAvg(1, 2);
        Assertions.assertTrue(logEvents.stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "Method name: calculateAvg, Args: [1, 2], Return value: 0.5")));
        Assertions.assertTrue(logEvents.stream()
                .anyMatch(event -> event.getLevel().equals(Level.DEBUG)));
    }
    @Test
    public void testAuditLogAnnotationWithDebugLevelOnCalculateAvgMethodWithGivenTwoParametersProvidingToIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> testService.calculateAvg(1, 0));
        for (LogEvent l: logEvents) {
            System.out.println(l.getMessage().getFormattedMessage());
        }
        Assertions.assertTrue(logEvents.stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "Method name: calculateAvg, Args: [1, 0]," +
                                " Exception occurred: java.lang.IllegalArgumentException: Possible zero division!")));
        Assertions.assertTrue(logEvents.stream()
                .anyMatch(event -> event.getLevel().equals(Level.DEBUG)));
    }

    @Test
    public void testAuditLogAnnotationWithInfoLevelOnPrintHelloMethodWithNoParameters() {
        testService.printHello();
        Assertions.assertTrue(logEvents.stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "Method name: printHello, No args, Return type: void")));
        Assertions.assertTrue(logEvents.stream()
                .anyMatch(event -> event.getLevel().equals(Level.INFO)));
    }

    @Test
    public void testAuditLogAnnotationWithWarnLevelOnThrowIllegalArgumentExceptionMethodWithNoParameters() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> testService.throwIllegalArgumentException());

        Assertions.assertTrue(logEvents.stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "Method name: throwIllegalArgumentException, No args," +
                                " Exception occurred: java.lang.IllegalArgumentException")));
        Assertions.assertTrue(logEvents.stream()
                .anyMatch(event -> event.getLevel().equals(Level.WARN)));
    }

}
