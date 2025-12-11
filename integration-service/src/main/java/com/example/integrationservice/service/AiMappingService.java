package com.example.integrationservice.service;

import com.example.integrationservice.dto.MappingRequestDto;
import com.example.integrationservice.dto.MappingResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class AiMappingService {
    private final ObjectMapper objectMapper;
    private final String ollamaHost;
    private final String ollamaModel;
    private final RestTemplate restTemplate;

    public AiMappingService(
            ObjectMapper objectMapper,
            RestTemplate restTemplate,
            @Value("${ollama.host:http://ollama:11434}") String ollamaHost,
            @Value("${ollama.model:mistral}") String ollamaModel
    ) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.ollamaHost = ollamaHost;
        this.ollamaModel = ollamaModel;
    }

    public MappingResponseDto mapData(MappingRequestDto request) {
        long startTime = System.currentTimeMillis();
        String mappingId = UUID.randomUUID().toString();

        try {
            log.info("Starting AI mapping with id: {}", mappingId);

            String prompt = buildMappingPrompt(request);
            String aiResponse = callOllamaApi(prompt);

            log.info("AI mapping response: {}", aiResponse);

            Map<String, Object> mappedData = parseMappedData(aiResponse, request);

            long executionTime = System.currentTimeMillis() - startTime;

            return MappingResponseDto.builder()
                    .mappingId(mappingId)
                    .status("SUCCESS")
                    .mappedData(mappedData)
                    .transformationDetails(aiResponse)
                    .processedAt(LocalDateTime.now())
                    .executionTimeMs(executionTime)
                    .build();

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Error during AI mapping", e);

            return MappingResponseDto.builder()
                    .mappingId(mappingId)
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .processedAt(LocalDateTime.now())
                    .executionTimeMs(executionTime)
                    .build();
        }
    }

    private String callOllamaApi(String prompt) throws Exception {
        String url = ollamaHost + "/api/generate";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", ollamaModel);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.3);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.debug("Calling Ollama API at: {}", url);
        try {
            Object response = restTemplate.postForObject(url, entity, Object.class);

            if (response instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) response;
                if (responseMap.containsKey("response")) {
                    return responseMap.get("response").toString();
                }
                throw new RuntimeException("Invalid response from Ollama API: missing 'response' field");
            } else if (response instanceof String) {
                String responseStr = (String) response;
                if (responseStr.startsWith("<")) {
                    throw new RuntimeException("Ollama returned HTML instead of JSON. The service may be unavailable or misconfigured.");
                }
                throw new RuntimeException("Unexpected response format from Ollama API: " + responseStr);
            }

            throw new RuntimeException("Invalid response from Ollama API: unexpected type " + response.getClass().getSimpleName());
        } catch (RestClientException e) {
            log.error("Failed to connect to Ollama API at {}: {}", url, e.getMessage());
            throw new RuntimeException("Ollama service is unavailable at " + url + ". Ensure the service is running and the model '" + ollamaModel + "' is downloaded. " +
                    "Run 'ollama pull " + ollamaModel + "' to download the model.", e);
        }
    }

    private String buildMappingPrompt(MappingRequestDto request) throws Exception {
        return String.format(
                """
                You are a data mapping expert. Map the following source data to the target schema.
                
                SOURCE SCHEMA:
                %s
                
                TARGET SCHEMA:
                %s
                
                SOURCE DATA:
                %s
                
                MAPPING RULES:
                %s
                
                Transform the source data according to the target schema and mapping rules. \
                Return ONLY valid JSON that matches the target schema. No explanation needed.""",
                request.getSourceSchema(),
                request.getTargetSchema(),
                objectMapper.writeValueAsString(request.getSourceData()),
                request.getMappingRules()
        );
    }

    private Map<String, Object> parseMappedData(String aiResponse, MappingRequestDto request) {
        try {
            String jsonString = aiResponse.trim();

            if (jsonString.startsWith("```json")) {
                jsonString = jsonString.substring(7);
            }
            if (jsonString.startsWith("```")) {
                jsonString = jsonString.substring(3);
            }
            if (jsonString.endsWith("```")) {
                jsonString = jsonString.substring(0, jsonString.length() - 3);
            }
            jsonString = jsonString.trim();

            return objectMapper.readValue(jsonString, Map.class);
        } catch (Exception e) {
            log.warn("Failed to parse AI response as JSON, returning raw response");
            return Map.of(
                    "raw_response", aiResponse,
                    "parse_error", e.getMessage()
            );
        }
    }
}
