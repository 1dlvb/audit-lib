package com.onedlvb.interceptor;

import com.onedlvb.advice.annotation.AuditLogHttp;
import com.onedlvb.util.LevelConverter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Http interceptor for logging HTTP-requests and HTTP-responses.
 * To activate logging you should annotate your controller method with @AuditLogHttp annotation.
 * To specify where logs should be printed configure auditlog.console.enabled=, auditlog.file.enabled=,
 * auditlog.file.path=logs/app.log variables.
 * @see com.onedlvb.advice.AuditLogAspect
 * @author Matushkin Anton
 */
@Component
public class HttpInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LogManager.getLogger(HttpInterceptor.class);

    /**
     * Pre-handle method that logs the current date and time along with the HTTP method of the request.
     *
     * @param request  The HTTP request
     * @param response The HTTP response
     * @param handler  The handler that will process the request
     * @return boolean This should always return true to ensure the request continues to be processed
     * @throws Exception
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        return true;
    }

    /**
     *  * After-completion method that logs the status code of the response along with any request or response bodies.
     * @param request   The HTTP request
     * @param response  The HTTP response
     * @param handler   The handler     that processed the request
     * @param ex        Any exception that was thrown during processing of the request
     * @throws Exception
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex)
            throws Exception {

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append(getCurrentDateTime())
                .append(" ")
                .append(request.getMethod());
        logBuilder.append(String.format(" Status code: %s", response.getStatus()));
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Object responseBody = requestAttributes != null
                ? requestAttributes.getAttribute("INTERCEPTED_RESPONSE_BODY", RequestAttributes.SCOPE_REQUEST) : null;
        Object requestBody = requestAttributes != null
                ? requestAttributes.getAttribute("INTERCEPTED_REQUEST_BODY", RequestAttributes.SCOPE_REQUEST) : null;

        if (requestBody != null) {
            logBuilder.append(" Request body: ").append(requestBody);
        }
        if (responseBody != null) {
            logBuilder.append(" Response body: ").append(responseBody);
        }
        LOGGER.log(getLogLevel(handler, AuditLogHttp.class), logBuilder);
    }

    /**
     * Utility method to get the current date and time formatted as "yyyy-MM-dd HH:mm:ss.SSS".
     *
     * @return String The current date and time formatted
     */
    private String getCurrentDateTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return localDateTime.format(formatter);
    }

    /**
     * Utility method to get the log level specified by the {@link AuditLogHttp} annotation on the handler.
     *
     * @param handler The handler (or controller method) that may have the @AuditLogHttp annotation
     * @param clazz   The @AuditLogHttp annotation class
     * @return Level The log level specified by the annotation, or DEBUG if not specified
     */
    private Level getLogLevel(Object handler, Class clazz) {
        if (handler instanceof HandlerMethod handlerMethod) {
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(clazz)) {
                AuditLogHttp auditLogHttp = method.getAnnotation(AuditLogHttp.class);
                return LevelConverter.convertLevel(auditLogHttp.logLevel());
            }
        }

        return Level.DEBUG;
    }

}
