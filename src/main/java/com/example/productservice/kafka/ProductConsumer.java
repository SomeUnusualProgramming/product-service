package com.example.productservice.kafka;

import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProductConsumer {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public ProductConsumer(ProductRepository productRepository, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "products", groupId = "product-events-group")
    public void consume(String message) {
        try {
            System.out.println("Kafka message received: " + message); // <- logujemy surowy JSON

            Product productEvent = objectMapper.readValue(message, Product.class);
            productEvent.setEventTime(LocalDateTime.now());

            Product history = new Product();
            history.setName(productEvent.getName());
            history.setDescription(productEvent.getDescription());
            history.setPrice(productEvent.getPrice());
            history.setEventType(productEvent.getEventType());
            history.setEventTime(productEvent.getEventTime());
            history.setOriginalProductId(productEvent.getId());

            System.out.println("History to save: " + history); // <- logujemy obiekt przed zapisem
            productRepository.save(history);
            System.out.println("History saved: id=" + history.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
