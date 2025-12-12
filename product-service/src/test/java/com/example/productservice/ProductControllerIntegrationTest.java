package com.example.productservice;

import com.example.productservice.kafka.ProductProducer;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1)
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private ProductProducer productProducer;

    private static final String TENANT_ID = "test-tenant";
    private static final String OTHER_TENANT_ID = "other-tenant";
    private static final String BASE_PATH = "/api/products";

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        TenantContext.clear();
    }

    @Nested
    @DisplayName("Endpoint Tests")
    class EndpointTests {

        @Test
        @DisplayName("GET /api/products - should return all products for tenant")
        void testGetAllProducts() throws Exception {
            createProductForTenant("Apple", "Fresh apple", "Fruit", 1.5, 100, TENANT_ID);
            createProductForTenant("Banana", "Yellow banana", "Fruit", 0.8, 150, TENANT_ID);

            mockMvc.perform(get(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name").value("Apple"))
                    .andExpect(jsonPath("$[1].name").value("Banana"));
        }

        @Test
        @DisplayName("GET /api/products - should return empty list when no products exist")
        void testGetAllProductsEmpty() throws Exception {
            mockMvc.perform(get(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("GET /api/products - should only return products for current tenant")
        void testGetAllProductsIsolatedByTenant() throws Exception {
            createProductForTenant("Apple", "Fresh apple", "Fruit", 1.5, 100, TENANT_ID);
            createProductForTenant("Carrot", "Orange carrot", "Vegetable", 0.5, 200, OTHER_TENANT_ID);

            mockMvc.perform(get(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].name").value("Apple"));
        }

        @Test
        @DisplayName("GET /api/products/{id} - should return product by id")
        void testGetProductById() throws Exception {
            Product product = createProductForTenant("Apple", "Fresh apple", "Fruit", 1.5, 100, TENANT_ID);

            mockMvc.perform(get(BASE_PATH + "/" + product.getId())
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Apple"))
                    .andExpect(jsonPath("$.price").value(1.5))
                    .andExpect(jsonPath("$.stockQuantity").value(100));
        }

        @Test
        @DisplayName("GET /api/products/{id} - should return 404 when product not found")
        void testGetProductByIdNotFound() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/999")
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"))
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("GET /api/products/{id}/history - should return product history")
        void testGetProductHistory() throws Exception {
            Product product = createProductForTenant("Apple", "Fresh apple", "Fruit", 1.5, 100, TENANT_ID);
            Product historyEntry = new Product();
            historyEntry.setOriginalProductId(product.getId());
            historyEntry.setName("Apple");
            historyEntry.setDescription("Fresh apple");
            historyEntry.setCategory("Fruit");
            historyEntry.setPrice(1.5);
            historyEntry.setStockQuantity(100);
            historyEntry.setEventType("CREATED");
            historyEntry.setEventTime(LocalDateTime.now());
            historyEntry.setTenantId(TENANT_ID);
            productRepository.save(historyEntry);

            mockMvc.perform(get(BASE_PATH + "/" + product.getId() + "/history")
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
        }

        @Test
        @DisplayName("POST /api/products - should create new product")
        void testCreateProduct() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Apple"))
                    .andExpect(jsonPath("$.price").value(1.5))
                    .andExpect(jsonPath("$.stockQuantity").value(100))
                    .andExpect(jsonPath("$.id").isNumber());
        }

        @Test
        @DisplayName("PUT /api/products/{id} - should update product")
        void testUpdateProduct() throws Exception {
            Product product = createProductForTenant("Apple", "Fresh apple", "Fruit", 1.5, 100, TENANT_ID);

            String updateJson = """
                    {
                        "name": "Updated Apple",
                        "description": "Very fresh apple",
                        "category": "Fruit",
                        "price": 2.0,
                        "stockQuantity": 80
                    }
                    """;

            mockMvc.perform(put(BASE_PATH + "/" + product.getId())
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Apple"))
                    .andExpect(jsonPath("$.price").value(2.0))
                    .andExpect(jsonPath("$.stockQuantity").value(80));
        }

        @Test
        @DisplayName("PUT /api/products/{id} - should return 404 when product not found")
        void testUpdateProductNotFound() throws Exception {
            String updateJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(put(BASE_PATH + "/999")
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("DELETE /api/products/{id} - should delete product")
        void testDeleteProduct() throws Exception {
            Product product = createProductForTenant("Apple", "Fresh apple", "Fruit", 1.5, 100, TENANT_ID);

            mockMvc.perform(delete(BASE_PATH + "/" + product.getId())
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get(BASE_PATH + "/" + product.getId())
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE /api/products/{id} - should return 404 when product not found")
        void testDeleteProductNotFound() throws Exception {
            mockMvc.perform(delete(BASE_PATH + "/999")
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("Security Layer Tests - X-Tenant-Id Validation")
    class SecurityLayerTests {

        @Test
        @DisplayName("GET /api/products - should reject request without X-Tenant-Id header")
        void testGetProductsWithoutTenantHeader() throws Exception {
            mockMvc.perform(get(BASE_PATH))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("TENANT_MISSING"))
                    .andExpect(jsonPath("$.message").value(containsString("Tenant ID")));
        }

        @Test
        @DisplayName("GET /api/products/{id} - should reject request without X-Tenant-Id header")
        void testGetProductByIdWithoutTenantHeader() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("TENANT_MISSING"));
        }

        @Test
        @DisplayName("POST /api/products - should reject request without X-Tenant-Id header")
        void testCreateProductWithoutTenantHeader() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("TENANT_MISSING"));
        }

        @Test
        @DisplayName("PUT /api/products/{id} - should reject request without X-Tenant-Id header")
        void testUpdateProductWithoutTenantHeader() throws Exception {
            String updateJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(put(BASE_PATH + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("TENANT_MISSING"));
        }

        @Test
        @DisplayName("DELETE /api/products/{id} - should reject request without X-Tenant-Id header")
        void testDeleteProductWithoutTenantHeader() throws Exception {
            mockMvc.perform(delete(BASE_PATH + "/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("TENANT_MISSING"));
        }

        @Test
        @DisplayName("GET /api/products - should reject request with blank X-Tenant-Id header")
        void testGetProductsWithBlankTenantId() throws Exception {
            mockMvc.perform(get(BASE_PATH)
                            .header("X-Tenant-Id", "   "))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("TENANT_MISSING"));
        }

        @Test
        @DisplayName("POST /api/products - should create product isolated to tenant")
        void testCreateProductIsolatedToTenant() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isCreated());

            mockMvc.perform(get(BASE_PATH)
                            .header("X-Tenant-Id", OTHER_TENANT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("GET /api/products/{id} - should not allow access to other tenant's products")
        void testCannotAccessOtherTenantsProduct() throws Exception {
            Product product = createProductForTenant("Apple", "Fresh apple", "Fruit", 1.5, 100, TENANT_ID);

            mockMvc.perform(get(BASE_PATH + "/" + product.getId())
                            .header("X-Tenant-Id", OTHER_TENANT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("PUT /api/products/{id} - should not allow updating other tenant's products")
        void testCannotUpdateOtherTenantsProduct() throws Exception {
            Product product = createProductForTenant("Apple", "Fresh apple", "Fruit", 1.5, 100, TENANT_ID);

            String updateJson = """
                    {
                        "name": "Updated Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 2.0,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(put(BASE_PATH + "/" + product.getId())
                            .header("X-Tenant-Id", OTHER_TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
        }

        @Test
        @DisplayName("DELETE /api/products/{id} - should not allow deleting other tenant's products")
        void testCannotDeleteOtherTenantsProduct() throws Exception {
            Product product = createProductForTenant("Apple", "Fresh apple", "Fruit", 1.5, 100, TENANT_ID);

            mockMvc.perform(delete(BASE_PATH + "/" + product.getId())
                            .header("X-Tenant-Id", OTHER_TENANT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));

            mockMvc.perform(get(BASE_PATH + "/" + product.getId())
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Error Case Tests - Validation")
    class ValidationErrorTests {

        @Test
        @DisplayName("POST /api/products - should reject product with missing name")
        void testCreateProductMissingName() throws Exception {
            String productJson = """
                    {
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors.name").exists());
        }

        @Test
        @DisplayName("POST /api/products - should reject product with missing description")
        void testCreateProductMissingDescription() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors.description").exists());
        }

        @Test
        @DisplayName("POST /api/products - should reject product with missing category")
        void testCreateProductMissingCategory() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "price": 1.5,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors.category").exists());
        }

        @Test
        @DisplayName("POST /api/products - should reject product with missing price")
        void testCreateProductMissingPrice() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors.price").exists());
        }

        @Test
        @DisplayName("POST /api/products - should reject product with missing stockQuantity")
        void testCreateProductMissingStockQuantity() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors.stockQuantity").exists());
        }

        @Test
        @DisplayName("POST /api/products - should reject product with zero price")
        void testCreateProductZeroPrice() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 0.0,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors.price").exists());
        }

        @Test
        @DisplayName("POST /api/products - should reject product with negative price")
        void testCreateProductNegativePrice() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": -1.5,
                        "stockQuantity": 100
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors.price").exists());
        }

        @Test
        @DisplayName("POST /api/products - should reject product with negative stockQuantity")
        void testCreateProductNegativeStockQuantity() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": -10
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors.stockQuantity").exists());
        }

        @Test
        @DisplayName("POST /api/products - should accept product with zero stockQuantity")
        void testCreateProductZeroStockQuantity() throws Exception {
            String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": 0
                    }
                    """;

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("PUT /api/products/{id} - should reject update with missing name")
        void testUpdateProductMissingName() throws Exception {
            Product product = createProductForTenant("Apple", "Fresh apple", "Fruit", 1.5, 100, TENANT_ID);

            String updateJson = """
                    {
                        "description": "Very fresh apple",
                        "category": "Fruit",
                        "price": 2.0,
                        "stockQuantity": 80
                    }
                    """;

            mockMvc.perform(put(BASE_PATH + "/" + product.getId())
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
        }
    }

    @Nested
    @DisplayName("Error Case Tests - HTTP Errors")
    class HttpErrorTests {

        @Test
        @DisplayName("PATCH /api/products - should reject unsupported HTTP method")
        void testUnsupportedHttpMethod() throws Exception {
            mockMvc.perform(patch(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
        }
    }

    @Nested
    @DisplayName("Error Case Tests - Exception Handling")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("POST /api/products - should handle invalid JSON")
        void testInvalidJsonFormat() throws Exception {
            String invalidJson = "{invalid json}";

            mockMvc.perform(post(BASE_PATH)
                            .header("X-Tenant-Id", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @DisplayName("GET /api/products/{id} - error response should include path")
        void testErrorResponseIncludesPath() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/999")
                            .header("X-Tenant-Id", TENANT_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.path").value(BASE_PATH + "/999"))
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"))
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    private Product createProductForTenant(String name, String description, String category,
                                           Double price, Integer stockQuantity, String tenantId) {
        TenantContext.setTenantId(tenantId);
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setCategory(category);
            product.setPrice(price);
            product.setStockQuantity(stockQuantity);
            product.setTenantId(tenantId);
            product.setEventTime(LocalDateTime.now());
            return productRepository.save(product);
        } finally {
            TenantContext.clear();
        }
    }
}
