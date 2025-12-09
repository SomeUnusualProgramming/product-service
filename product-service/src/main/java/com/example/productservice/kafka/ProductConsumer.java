package com.example.productservice.kafka;

import com.example.productservice.constant.AppConstants;
import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class ProductConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ProductConsumer.class);
    private final ObjectMapper objectMapper;
    private final ProductService productService;

    public ProductConsumer(ObjectMapper objectMapper, ProductService productService) {
        this.objectMapper = objectMapper;
        this.productService = productService;
    }

    @KafkaListener(topics = {AppConstants.Kafka.TOPIC_PRODUCTS_LEGACY, AppConstants.Kafka.TOPIC_PRODUCTS},
                   groupId = AppConstants.Kafka.GROUP_ID_COMBINED)
    public void handleProductEvent(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            logger.info(AppConstants.Logger.KAFKA_MESSAGE_RECEIVED, topic, message);
            Product productEvent = objectMapper.readValue(message, Product.class);

            boolean processEvent = AppConstants.Kafka.TOPIC_PRODUCTS.equals(topic);
            productService.handleProductEventFromKafka(productEvent, processEvent);
        } catch (Exception e) {
            logger.error(AppConstants.Logger.ERROR_KAFKA_DESERIALIZE, topic, e);
        }
    }
}
