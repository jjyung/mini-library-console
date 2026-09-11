package com.example.library.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String TRACE_ID_ATTRIBUTE = CorrelationIdFilter.class.getName() + ".traceId";
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String headerValue = request.getHeader(CORRELATION_HEADER);
        String traceId = headerValue == null || headerValue.isBlank()
                ? UUID.randomUUID().toString()
                : headerValue.trim();
        request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
        response.setHeader(CORRELATION_HEADER, traceId);
        filterChain.doFilter(request, response);
    }
}
