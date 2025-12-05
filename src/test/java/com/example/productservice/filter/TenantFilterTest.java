package com.example.productservice.filter;

import com.example.productservice.security.TenantContext;
import com.example.productservice.security.TenantValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private TenantValidator tenantValidator;

    private TenantFilter tenantFilter;
    private ObjectMapper objectMapper;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        tenantFilter = new TenantFilter(tenantValidator, objectMapper);
        TenantContext.clear();

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testDoFilterWithValidTenantId() throws IOException, ServletException {
        String tenantId = "test-tenant";
        when(request.getHeader("X-Tenant-Id")).thenReturn(tenantId);
        when(request.getRequestURI()).thenReturn("/api/products");
        doNothing().when(tenantValidator).validateTenantId(tenantId);

        tenantFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(tenantValidator, times(1)).validateTenantId(tenantId);
        assertEquals(null, TenantContext.getTenantId());
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilterWithMissingTenantId() throws IOException, ServletException {
        when(request.getHeader("X-Tenant-Id")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/products");

        tenantFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(400);
        verify(filterChain, never()).doFilter(request, response);
        String responseBody = responseWriter.toString();
        assertTrue(responseBody.contains("TENANT_MISSING"));
    }

    @Test
    void testDoFilterWithBlankTenantId() throws IOException, ServletException {
        when(request.getHeader("X-Tenant-Id")).thenReturn("   ");
        when(request.getRequestURI()).thenReturn("/api/products");

        tenantFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(400);
        verify(filterChain, never()).doFilter(request, response);
        String responseBody = responseWriter.toString();
        assertTrue(responseBody.contains("TENANT_MISSING"));
    }

    @Test
    void testDoFilterWithInvalidTenantId() throws IOException, ServletException {
        String invalidTenantId = "invalid@tenant";
        when(request.getHeader("X-Tenant-Id")).thenReturn(invalidTenantId);
        when(request.getRequestURI()).thenReturn("/api/products");
        doThrow(new IllegalArgumentException("Invalid tenant ID format")).when(tenantValidator).validateTenantId(invalidTenantId);

        tenantFilter.doFilter(request, response, filterChain);

        verify(response).setStatus(400);
        verify(filterChain, never()).doFilter(request, response);
        String responseBody = responseWriter.toString();
        assertTrue(responseBody.contains("INVALID_TENANT_ID"));
    }

    @Test
    void testTenantContextClearedAfterFilter() throws IOException, ServletException {
        String tenantId = "test-tenant";
        when(request.getHeader("X-Tenant-Id")).thenReturn(tenantId);
        when(request.getRequestURI()).thenReturn("/api/products");
        doNothing().when(tenantValidator).validateTenantId(tenantId);

        tenantFilter.doFilter(request, response, filterChain);

        assertNull(TenantContext.getTenantId());
    }
}
