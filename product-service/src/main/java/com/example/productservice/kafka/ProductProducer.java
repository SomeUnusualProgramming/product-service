package com.example.productservice.kafka;

import com.example.productservice.constant.AppConstants;
import com.example.productservice.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
public class ProductProducer {

    private static final Logger logger = LoggerFactory.getLogger(ProductProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProductProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(Product product) {
        try {
            String json = objectMapper.writeValueAsString(product);

            kafkaTemplate.send(AppConstants.Kafka.TOPIC_PRODUCTS, json).whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info(AppConstants.Logger.MESSAGE_SENT, AppConstants.Kafka.TOPIC_PRODUCTS, json);
                } else {
                    logger.error(AppConstants.Logger.MESSAGE_SEND_FAILED, AppConstants.Kafka.TOPIC_PRODUCTS, json, ex);
                }
            });
        } catch (Exception e) {
            logger.error(AppConstants.Logger.ERROR_SERIALIZING, e);
        }
    }
}
