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
            System.out.println("Kafka message received: " + message);

            Product productEvent = objectMapper.readValue(message, Product.class);
            productEvent.setEventTime(LocalDateTime.now());

            Product history = new Product();
            history.setName(productEvent.getName());
            history.setDescription(productEvent.getDescription());
            history.setPrice(productEvent.getPrice());
            history.setEventType(productEvent.getEventType());
            history.setEventTime(productEvent.getEventTime());
            history.setOriginalProductId(productEvent.getId());

            System.out.println("History to save: " + history);
            productRepository.save(history);
            System.out.println("History saved: id=" + history.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = "products-topic", groupId = "product-service-group")
    public void listen(String message) {
        try {
            System.out.println("Kafka event received: " + message);

            Product productEvent = objectMapper.readValue(message, Product.class);
            productEvent.setEventTime(LocalDateTime.now());

            Product history = new Product();
            history.setName(productEvent.getName());
            history.setDescription(productEvent.getDescription());
            history.setCategory(productEvent.getCategory());
            history.setPrice(productEvent.getPrice());
            history.setStockQuantity(productEvent.getStockQuantity());
            history.setEventType(productEvent.getEventType());
            history.setEventTime(productEvent.getEventTime());
            history.setOriginalProductId(productEvent.getId());

            productRepository.save(history);
            System.out.println("History saved: id=" + history.getId());

            switch (productEvent.getEventType()) {
                case "CREATED" -> {
                    System.out.println("Handling CREATED event");
                    productRepository.save(productEvent);
                }
                case "UPDATED" -> {
                    System.out.println("Handling UPDATED event");
                    productRepository.findById(productEvent.getId()).ifPresent(existing -> {
                        existing.setName(productEvent.getName());
                        existing.setDescription(productEvent.getDescription());
                        existing.setCategory(productEvent.getCategory());
                        existing.setPrice(productEvent.getPrice());
                        existing.setStockQuantity(productEvent.getStockQuantity());
                        productRepository.save(existing);
                    });
                }
                case "DELETED" -> {
                    System.out.println("Handling DELETED event");
                    productRepository.deleteById(productEvent.getId());
                }
                default -> System.out.println("Unknown event type: " + productEvent.getEventType());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
