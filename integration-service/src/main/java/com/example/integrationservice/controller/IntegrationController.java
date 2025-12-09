package com.example.integrationservice.controller;

import com.example.integrationservice.dto.MappingRequestDto;
import com.example.integrationservice.dto.MappingResponseDto;
import com.example.integrationservice.service.AiMappingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    private final AiMappingService aiMappingService;

    public IntegrationController(AiMappingService aiMappingService) {
        this.aiMappingService = aiMappingService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "integration-service");
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("version", "0.0.1");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/ai/map")
    public ResponseEntity<MappingResponseDto> mapDataWithAi(@RequestBody MappingRequestDto request) {
        MappingResponseDto response = aiMappingService.mapData(request);
        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/sync/products")
    public ResponseEntity<Map<String, Object>> syncProducts() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Product sync initiated");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "PROCESSING");
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> getDetailedHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("components", Map.of(
            "database", Map.of("status", "UP"),
            "kafka", Map.of("status", "UP"),
            "productService", Map.of("status", "UP", "url", "http://product-service:8080")
        ));
        health.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(health);
    }

    @PostMapping("/test/message")
    public ResponseEntity<Map<String, String>> sendTestMessage() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test message sent to Kafka");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
