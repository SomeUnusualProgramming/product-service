package com.example.productservice;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductConsumer {

    @KafkaListener(topics = "products-topic", groupId = "product-group")
    public void consume(String message) {
        System.out.println("Received message: " + message);
    }
}
