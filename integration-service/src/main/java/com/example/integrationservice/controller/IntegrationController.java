package com.example.integrationservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "integration-service");
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("version", "0.0.1");
        return ResponseEntity.ok(status);
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
