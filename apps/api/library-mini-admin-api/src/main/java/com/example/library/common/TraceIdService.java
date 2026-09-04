package com.example.library.common;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TraceIdService {

    public static final String TRACE_ID_ATTRIBUTE = TraceIdService.class.getName() + ".traceId";

    public String getOrCreateTraceId() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return UUID.randomUUID().toString();
        }

        Object traceIdAttribute = requestAttributes.getAttribute(TRACE_ID_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if (traceIdAttribute instanceof String traceId && !traceId.isBlank()) {
            return traceId;
        }

        String traceId = UUID.randomUUID().toString();
        requestAttributes.setAttribute(TRACE_ID_ATTRIBUTE, traceId, RequestAttributes.SCOPE_REQUEST);
        return traceId;
    }
}
