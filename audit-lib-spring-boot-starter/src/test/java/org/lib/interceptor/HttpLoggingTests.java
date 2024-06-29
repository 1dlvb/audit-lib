package org.lib.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lib.TestConfig;
import org.lib.advice.annotation.AuditLogHttp;
import org.lib.appender.TestAppender;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {
        TestConfig.class,
        HttpLoggingTests.AuditLogHttpTestController.class,
})
@AutoConfigureMockMvc
@WebMvcTest(controllers = HttpLoggingTests.AuditLogHttpTestController.class)
public class HttpLoggingTests {


    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TestAppender.getLogEvents().clear();
    }

    @Test
    void testGetMappingWithNoArgsReturnsStringStatus200() throws Exception {
        mockMvc.perform(get("/get-with-no-args")).andExpect(status().isOk());
        List<LogEvent> logEvents = TestAppender.getLogEvents();
        assertThat(TestAppender.getLogEvents()).isNotEmpty();
        for (LogEvent l: logEvents) {
            System.out.println(l.getMessage().getFormattedMessage());
        }
        assertTrue(logEvents.stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "GET Status code: 200 Response body: Test Audit Log")));
        assertTrue(logEvents.stream()
                .anyMatch(event -> event.getLevel().equals(Level.DEBUG)));
    }

    @Test
    void testPostRequestToGetMappingWithNoArgsReturnsStringStatus405() throws Exception {
        mockMvc.perform(post("/get-with-no-args", "Argument"));
        List<LogEvent> logEvents = TestAppender.getLogEvents();
        assertThat(TestAppender.getLogEvents()).isNotEmpty();
        for (LogEvent l: logEvents) {
            System.out.println(l.getMessage().getFormattedMessage());
        }

        assertTrue(logEvents.stream()
                .anyMatch(event -> event.getMessage().getFormattedMessage().contains(
                        "POST Status code: 405")));
        assertTrue(logEvents.stream()
                .anyMatch(event -> event.getLevel().equals(Level.DEBUG)));
    }

    @RestController
    static class AuditLogHttpTestController {

        @AuditLogHttp
        @GetMapping("/get-with-no-args")
        public String getMappingWithNoArgsReturnsStringMethod() {
            return "Test Audit Log";
        }

        @AuditLogHttp
        @GetMapping("/post-with-args")
        public String postMappingWithStringAsArgumentReturnsStringMethod(String arg) {
            return arg;
        }
    }
}
