// language: java
package com.example.productservice.controller;

import com.example.productservice.kafka.ProductProducer;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private ProductProducer productProducer;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void testProductNotFoundExceptionReturns404() throws Exception {
        mockMvc.perform(get("/api/products/999")
                        .header("X-Tenant-Id", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("not found")))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/products/999"));
    }

    @Test
    void testProductNotFoundOnUpdateReturns404() throws Exception {
        String productJson = """
                {
                    "name": "Updated Product",
                    "description": "Updated description",
                    "category": "UpdatedCategory",
                    "price": 99.99,
                    "stockQuantity": 50
                }
                """;

        mockMvc.perform(put("/api/products/999")
                        .header("X-Tenant-Id", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void testProductNotFoundOnDeleteReturns404() throws Exception {
        mockMvc.perform(delete("/api/products/999")
                        .header("X-Tenant-Id", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void testValidationErrorReturns400() throws Exception {
        String invalidProductJson = """
                {
                    "name": "",
                    "description": "Missing category and price",
                    "category": "",
                    "price": -10,
                    "stockQuantity": -5
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-Tenant-Id", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidProductJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.category").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists())
                .andExpect(jsonPath("$.fieldErrors.stockQuantity").exists());
    }

    @Test
    void testMissingRequiredFieldsReturns400() throws Exception {
        String incompleteProductJson = """
                {
                    "name": "Product"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-Tenant-Id", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteProductJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void testPricePositiveValidationReturns400() throws Exception {
        String productWithZeroPriceJson = """
                {
                    "name": "Product",
                    "description": "Description",
                    "category": "Category",
                    "price": 0.0,
                    "stockQuantity": 10
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-Tenant-Id", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productWithZeroPriceJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.price").value(containsString("greater than 0")));
    }

    @Test
    void testStockQuantityNonNegativeValidationReturns400() throws Exception {
        String productWithNegativeStockJson = """
                {
                    "name": "Product",
                    "description": "Description",
                    "category": "Category",
                    "price": 10.0,
                    "stockQuantity": -5
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-Tenant-Id", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productWithNegativeStockJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.stockQuantity").value(containsString("cannot be negative")));
    }

    @Test
    void testValidProductCreationSucceeds() throws Exception {
        String validProductJson = """
                {
                    "name": "Valid Product",
                    "description": "Valid description",
                    "category": "Valid Category",
                    "price": 10.0,
                    "stockQuantity": 50
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-Tenant-Id", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Valid Product"))
                .andExpect(jsonPath("$.price").value(10.0))
                .andExpect(jsonPath("$.stockQuantity").value(50));
    }

    @Test
    void testMissingTenantIdReturns400() throws Exception {
        String validProductJson = """
                {
                    "name": "Valid Product",
                    "description": "Valid description",
                    "category": "Valid Category",
                    "price": 10.0,
                    "stockQuantity": 50
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("TENANT_MISSING"))
                .andExpect(jsonPath("$.message").value(containsString("Tenant ID")));
    }

    @Test
    void testBlankTenantIdReturns400() throws Exception {
        String validProductJson = """
                {
                    "name": "Valid Product",
                    "description": "Valid description",
                    "category": "Valid Category",
                    "price": 10.0,
                    "stockQuantity": 50
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-Tenant-Id", "   ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("TENANT_MISSING"));
    }

    @Test
    void testInvalidTenantIdFormatReturns400() throws Exception {
        String validProductJson = """
                {
                    "name": "Valid Product",
                    "description": "Valid description",
                    "category": "Valid Category",
                    "price": 10.0,
                    "stockQuantity": 50
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-Tenant-Id", "invalid@tenant#")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProductJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Invalid tenant ID")));
    }
}
