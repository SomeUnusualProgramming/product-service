package com.example.productservice.filter;

import com.example.productservice.dto.ErrorResponse;
import com.example.productservice.exception.TenantMissingException;
import com.example.productservice.security.TenantContext;
import com.example.productservice.security.TenantValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TenantFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(TenantFilter.class);
    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final TenantValidator tenantValidator;
    private final ObjectMapper objectMapper;

    public TenantFilter(TenantValidator tenantValidator, ObjectMapper objectMapper) {
        this.tenantValidator = tenantValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestUri = httpRequest.getRequestURI();

        if (shouldSkipTenantValidation(requestUri)) {
            chain.doFilter(request, response);
            return;
        }

        String tenantId = httpRequest.getHeader(TENANT_HEADER);

        if (tenantId == null || tenantId.isBlank()) {
            logger.warn("Missing tenant ID header in request: {}", requestUri);
            sendErrorResponse(httpResponse, HttpStatus.BAD_REQUEST, "TENANT_MISSING",
                    "Tenant ID is required. Please provide X-Tenant-Id header.", requestUri);
            return;
        }

        try {
            tenantValidator.validateTenantId(tenantId);
            TenantContext.setTenantId(tenantId);
            logger.debug("Set tenant context for tenant: {}", tenantId);
            chain.doFilter(request, response);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid tenant ID format: {}", tenantId);
            sendErrorResponse(httpResponse, HttpStatus.BAD_REQUEST, "INVALID_TENANT_ID",
                    ex.getMessage(), requestUri);
        } finally {
            TenantContext.clear();
        }
    }

    private boolean shouldSkipTenantValidation(String requestUri) {
        return requestUri.equals("/") ||
               requestUri.equals("/index.html") ||
               requestUri.startsWith("/static/") ||
               requestUri.startsWith("/css/") ||
               requestUri.startsWith("/js/") ||
               requestUri.startsWith("/images/") ||
               requestUri.startsWith("/favicon") ||
               requestUri.endsWith(".css") ||
               requestUri.endsWith(".js") ||
               requestUri.endsWith(".png") ||
               requestUri.endsWith(".jpg") ||
               requestUri.endsWith(".jpeg") ||
               requestUri.endsWith(".gif") ||
               requestUri.endsWith(".svg") ||
               requestUri.endsWith(".ico") ||
               requestUri.endsWith(".woff") ||
               requestUri.endsWith(".woff2") ||
               requestUri.endsWith(".ttf");
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String errorCode,
                                   String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                errorCode,
                message,
                path
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
