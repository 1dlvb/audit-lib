package org.lib.advice;

import org.lib.advice.annotation.AuditLogHttp;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * The CustomRequestBodyAdvice class is used to intercept the HTTP request body before it is passed to the controller.
 * This class specifically checks for the presence of the @AuditLogHttp annotation on controller methods to determine
 * if the request should be logged.
 * @author Matushkin Anton
 */
@ControllerAdvice
public class CustomRequestBodyAdvice implements RequestBodyAdvice {

    /**
     * Determines if the advice is applicable to the method based on the presence of the @AuditLogHttp annotation.
     *
     * @param methodParameter The method parameter
     * @param targetType      The target type of the body
     * @param converterType   The selected converter type
     * @return boolean        True if the method parameter is annotated with @AuditLogHttp, false otherwise
     */
    @Override
    public boolean supports(MethodParameter methodParameter, @NonNull Type targetType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(AuditLogHttp.class);
    }

    /**
     * @param inputMessage   The HTTP input message
     * @param parameter      The method parameter
     * @param targetType     The target type
     * @param converterType  The converter type
     * @return HttpInputMessage The input message, possibly modified
     * @throws IOException
     */
    @NonNull
    @Override
    public HttpInputMessage beforeBodyRead(@NonNull HttpInputMessage inputMessage,
                                           @NonNull MethodParameter parameter,
                                           @NonNull Type targetType,
                                           @NonNull Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return inputMessage;
    }

    /**
     * Invoked after the body has been read from the input message,
     * intercepting and sets requestBody as attribute.
     *
     * @param body          The body read from the input message
     * @param inputMessage  The HTTP input message
     * @param parameter     The method parameter
     * @param targetType    The target type
     * @param converterType The converter type
     * @return Object       The same body
     */
    @NonNull
    @Override
    public Object afterBodyRead(@NonNull Object body,
                                @NonNull HttpInputMessage inputMessage,
                                @NonNull MethodParameter parameter,
                                @NonNull Type targetType,
                                @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        requestAttributes.setAttribute("INTERCEPTED_REQUEST_BODY", body, RequestAttributes.SCOPE_REQUEST);
        return body;
    }

    /**
     * Invoked when the body is empty.
     *
     * @param body          The body, which may be null
     * @param inputMessage  The HTTP input message
     * @param parameter     The method parameter
     * @param targetType    The target type
     * @param converterType The converter type
     * @return Object       The default or modified body
     */
    @Override
    public Object handleEmptyBody(Object body, @NonNull HttpInputMessage inputMessage,
                                  @NonNull MethodParameter parameter,
                                  @NonNull Type targetType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

}
