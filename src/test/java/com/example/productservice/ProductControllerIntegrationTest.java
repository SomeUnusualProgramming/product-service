package com.example.productservice;

import com.example.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test") // używa application-test.properties
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class,})
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll(); // czyszczenie bazy przed każdym testem
    }

    @Test
    void testCreateAndGetProducts() throws Exception {
        // Tworzenie produktu
        String productJson = """
                    {
                        "name": "Apple",
                        "description": "Fresh apple",
                        "category": "Fruit",
                        "price": 1.5,
                        "stockQuantity": 100
                    }
                """;

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(productJson)).andExpect(status().isOk());

        // Sprawdzenie pobrania produktów
        mockMvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Apple"));
    }
}
