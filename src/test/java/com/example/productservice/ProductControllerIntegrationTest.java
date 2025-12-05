package com.example.productservice;

import com.example.productservice.kafka.ProductProducer;
import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class ProductControllerIntegrationTest {

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
    void testCreateAndGetProducts() throws Exception {
        String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": 100
                    }
                """;

        mockMvc.perform(post("/api/products")
                        .header("X-Tenant-Id", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products")
                        .header("X-Tenant-Id", "test-tenant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apple"));
    }
}
