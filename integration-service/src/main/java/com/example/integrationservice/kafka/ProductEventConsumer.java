package com.example.integrationservice.kafka;

import com.example.integrationservice.dto.MappingRequestDto;
import com.example.integrationservice.dto.MappingResponseDto;
import com.example.integrationservice.service.AiMappingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ProductEventConsumer {
    private final AiMappingService aiMappingService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ProductEventConsumer(
            AiMappingService aiMappingService,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.aiMappingService = aiMappingService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "product-events",
            groupId = "ai-mapping-group",
            autoStartup = "${kafka.listener.auto-start:true}"
    )
    public void consumeProductEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        try {
            log.info("Received product event from topic: {}", topic);
            log.debug("Event message: {}", message);

            Map<String, Object> sourceData = objectMapper.readValue(message, Map.class);

            MappingRequestDto mappingRequest = MappingRequestDto.builder()
                    .sourceData(sourceData)
                    .sourceSchema(getProductSourceSchema())
                    .targetSchema(getIntegrationTargetSchema())
                    .mappingRules(getDefaultMappingRules())
                    .build();

            MappingResponseDto response = aiMappingService.mapData(mappingRequest);

            if ("SUCCESS".equals(response.getStatus())) {
                log.info("Successfully mapped product data with id: {}", response.getMappingId());
                publishMappedData(response.getMappedData());
            } else {
                log.error("Mapping failed: {}", response.getErrorMessage());
                publishMappingError(response);
            }

        } catch (Exception e) {
            log.error("Error processing product event", e);
        }
    }

    private String getProductSourceSchema() {
        return """
                {
                  "id": "UUID",
                  "name": "String",
                  "description": "String",
                  "price": "BigDecimal",
                  "stock": "Integer",
                  "created_at": "LocalDateTime",
                  "updated_at": "LocalDateTime"
                }
                """;
    }

    private String getIntegrationTargetSchema() {
        return """
                {
                  "product_id": "UUID",
                  "product_name": "String",
                  "product_description": "String",
                  "unit_price": "BigDecimal",
                  "available_stock": "Integer",
                  "created_timestamp": "LocalDateTime",
                  "updated_timestamp": "LocalDateTime",
                  "integration_status": "String"
                }
                """;
    }

    private String getDefaultMappingRules() {
        return """
                - Map 'id' to 'product_id'
                - Map 'name' to 'product_name'
                - Map 'description' to 'product_description'
                - Map 'price' to 'unit_price'
                - Map 'stock' to 'available_stock'
                - Map 'created_at' to 'created_timestamp'
                - Map 'updated_at' to 'updated_timestamp'
                - Set 'integration_status' to 'SYNCED'
                """;
    }

    private void publishMappedData(Map<String, Object> mappedData) {
        try {
            String message = objectMapper.writeValueAsString(mappedData);
            kafkaTemplate.send("product-integration-mapped", message);
            log.info("Published mapped product data to Kafka");
        } catch (Exception e) {
            log.error("Error publishing mapped data", e);
        }
    }

    private void publishMappingError(MappingResponseDto response) {
        try {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("mapping_id", response.getMappingId());
            errorData.put("error_message", response.getErrorMessage());
            errorData.put("status", "FAILED");

            String message = objectMapper.writeValueAsString(errorData);
            kafkaTemplate.send("product-integration-errors", message);
            log.info("Published mapping error to Kafka");
        } catch (Exception e) {
            log.error("Error publishing mapping error", e);
        }
    }
}
