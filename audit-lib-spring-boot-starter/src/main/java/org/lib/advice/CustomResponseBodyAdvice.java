package org.lib.advice;

import org.lib.advice.annotation.AuditLogHttp;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * The CustomResponseBodyAdvice class is used to intercept the HTTP response body before it is passed to the controller.
 * This class specifically checks for the presence of the @AuditLogHttp annotation on controller methods to determine
 * if the response should be logged.
 * @author Matushkin Anton
 */
@ControllerAdvice
public class CustomResponseBodyAdvice implements ResponseBodyAdvice<Object> {


    /**
     * Determines if the advice is applicable to the controller method based on the presence of the @AuditLogHttp annotation.
     *
     * @param returnType     The type of the value being returned from the controller method
     * @param converterType  The HttpMessageConverter type
     * @return boolean       True if the controller method is annotated with @AuditLogHttp, false otherwise
     */
    @Override
    public boolean supports(MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {

        return returnType.hasMethodAnnotation(AuditLogHttp.class);
    }

    /**
     * Invoked after the body has been read from the input message,
     * intercepting and sets responseBody as attribute.
     *
     * @param body                The body that will be written to the response
     * @param returnType          The type of the value being returned from the controller method
     * @param selectedContentType The content type selected for the response
     * @param selectedConverterType The HttpMessageConverter type
     * @param request             The server request
     * @param response            The server response
     * @return Object             The body that will be written
     */
    @Override
    public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {

        if (request instanceof ServletServerHttpRequest) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                requestAttributes.setAttribute("INTERCEPTED_RESPONSE_BODY", body, RequestAttributes.SCOPE_REQUEST);
            }
        }
        return body;
    }

}
