package com.example.productservice;

import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Profile("!test") // NIE ładuj tego beana w profilu test
@Component
public class ProductProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "products-topic";

    public ProductProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
    }
}
