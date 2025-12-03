package com.example.productservice.kafka;

import com.example.productservice.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
public class ProductProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC = "products-topic";

    public ProductProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(Product product) {
        try {
            String json = objectMapper.writeValueAsString(product);

            kafkaTemplate.send(TOPIC, json).whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Message sent: " + json);
                } else {
                    System.err.println("Failed to send message: " + json);
                    ex.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
