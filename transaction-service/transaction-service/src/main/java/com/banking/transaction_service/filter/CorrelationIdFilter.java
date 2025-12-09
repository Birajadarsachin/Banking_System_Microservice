package com.banking.transaction_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Read correlation ID from incoming request header
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        // If not provided, generate one
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Put into MDC so logback can print it
        MDC.put("correlationId", correlationId);

        try {
            // Continue request processing
            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC to avoid memory leaks
            MDC.remove("correlationId");
        }
    }
}
