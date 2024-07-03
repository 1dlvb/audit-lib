package com.onedlvb.interceptor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.onedlvb.advice.annotation.AuditLogHttp;
import com.onedlvb.appender.TestAppender;
import com.onedlvb.config.AuditLibSpringBootStarterAutoConfiguration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@WebMvcTest(HttpLoggingTests.AuditLogHttpTestController.class)
@ContextConfiguration(classes = {HttpLoggingTests.AuditLogHttpTestController.class, AuditLibSpringBootStarterAutoConfiguration.class})
public class HttpLoggingTests {

    @Autowired
    private MockMvc mockMvc;

    private TestAppender testAppender;

    @BeforeEach
    public void setUp() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        testAppender = new TestAppender("TestAppender");
        testAppender.start();

        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.addAppender(testAppender, Level.DEBUG, null);
        context.updateLoggers();
    }

    @AfterEach
    public void tearDown() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.removeAppender("TestAppender");
        testAppender.stop();
    }

    @Test
    void testGetMappingWithNoArgsReturnsStringStatus200() throws Exception {
        mockMvc.perform(get("/get-with-no-args"))
                .andExpect(status().isOk())
                .andExpect(content().string("Test Audit Log"));
        List<String> logMessages = TestAppender.getLogEvents().stream()
                .map(event -> event.getMessage().getFormattedMessage())
                .toList();
        Assertions.assertTrue(logMessages.stream().anyMatch(message ->
                        message.contains("GET Status code: 200 Response body: Test Audit Log")),
                "Expected log message not found");
    }

    @Test
    void test404NotFound() throws Exception {
        mockMvc.perform(get("/404-not-found"))
                .andExpect(status().isNotFound());
        List<String> logMessages = TestAppender.getLogEvents().stream()
                .map(event -> event.getMessage().getFormattedMessage())
                .toList();
        Assertions.assertTrue(logMessages.stream().anyMatch(message ->
                        message.contains("GET Status code: 404")),
                "Expected log message not found");
    }

    @Test
    void testGetMappingWithArgsReturnsDouble() throws Exception {
        mockMvc.perform(get("/get-with-two-args-returns-double")
                        .param("arg1", "1")
                        .param("arg2", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("0.5"));

        for (LogEvent l: TestAppender.getLogEvents()) {
            System.out.println(l.getMessage().getFormattedMessage());
        }
        List<String> logMessages = TestAppender.getLogEvents().stream()
                .map(event -> event.getMessage().getFormattedMessage())
                .toList();
        Assertions.assertTrue(logMessages.stream().anyMatch(message ->
                        message.contains("GET Status code: 200 Response body: 0.5")),
                "Expected log message not found");
    }
    @RestController
    static class AuditLogHttpTestController {
        @AuditLogHttp
        @GetMapping("/get-with-no-args")
        public String getMappingWithNoArgsReturnsStringMethod() {
            return "Test Audit Log";
        }

        @AuditLogHttp
        @GetMapping("/get-with-two-args-returns-double")
        public String getMappingWithNoArgsReturnsStringMethod(@RequestParam("arg1") Integer arg1,
                                                              @RequestParam("arg2") Integer arg2) {
            return String.valueOf((double)arg1/arg2)    ;
        }


    }

}

//    @Test
//    void testPostRequestToGetMappingWithNoArgsReturnsStringStatus405() throws Exception {
//        mockMvc.perform(post("/get-with-no-args", "Argument"));
//        List<LogEvent> logEvents = TestAppender.getLogEvents();
//        assertThat(TestAppender.getLogEvents()).isNotEmpty();
//        for (LogEvent l: logEvents) {
//            System.out.println(l.getMessage().getFormattedMessage());
//        }
//
//        assertTrue(logEvents.stream()
//                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
//                        "POST Status code: 405")));
//        assertTrue(logEvents.stream()
//                .anyMatch(event -> event.getLevel().equals(Level.DEBUG)));
//    }
//
