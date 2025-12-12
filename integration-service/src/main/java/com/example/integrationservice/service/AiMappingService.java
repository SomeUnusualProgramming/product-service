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
import java.util.LinkedHashMap;
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
            log.debug("Starting AI mapping with id: {} for data: {}", mappingId, request.getSourceData());

            String prompt = buildMappingPrompt(request);
            String aiResponse = callOllamaApi(prompt);

            log.debug("AI mapping response for id {}: {}", mappingId, aiResponse);

            Map<String, Object> mappedData = parseMappedData(aiResponse, request);
            
            if (mappedData == null || mappedData.isEmpty()) {
                log.warn("AI mapping returned empty or null data for id: {}", mappingId);
            }

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
            log.error("Error during AI mapping with id: {}", mappingId, e);

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
        String targetSampleSection = "";
        if (request.getTargetSampleData() != null && !request.getTargetSampleData().isEmpty()) {
            targetSampleSection = "\nTARGET SAMPLE DATA (structure example):\n" + request.getTargetSampleData();
        }
        
        return String.format(
                """
                CRITICAL: Return ONLY a JSON object. Do NOT include any introduction, explanation, text, or commentary. Start directly with { and end with }.
                
                You are a data mapping expert. Map the following source data to the target schema.
                
                SOURCE SCHEMA (note the data types):
                %s
                
                TARGET SCHEMA:
                %s
                %s
                
                SOURCE DATA:
                %s
                
                MAPPING RULES:
                %s
                
                INSTRUCTIONS FOR JSON GENERATION:
                1. Return ONLY a valid JSON object that EXACTLY matches the target schema structure
                2. Start with { and end with } - no text before or after
                3. IMPORTANT: Preserve data types from SOURCE SCHEMA when mapping:
                   - If source field is "List" type, map it as JSON array [] even if target name is different
                   - If source is array/list, the mapped target MUST also be an array
                   - Example: if "categories" is List type, "category" field should be ["value1","value2"], NOT "value1,value2"
                4. CRITICAL FOR COMPLEX TYPES - Handle nested objects correctly:
                   - NEVER stringify objects or arrays - return them as proper JSON structures
                   - If source has nested object like {length: 30, width: 25, height: 2}, return it as: {"length": 30, "width": 25, "height": 2}
                   - WRONG: "dimensions": "{ \"length\": '30', \"width\": '25' }"
                   - CORRECT: "dimensions": {"length": 30, "width": 25}
                   - NEVER wrap objects or arrays in quotes
                5. Look at TARGET SAMPLE DATA to understand the correct structure
                6. If target sample shows a field as array, return it as JSON array, NOT as string
                7. If target sample shows a field as object/nested, return it as JSON object, NOT as string
                8. If target sample shows a field as number, return it as number, NOT as string
                9. Match the EXACT structure and types shown in TARGET SAMPLE DATA and SOURCE SCHEMA
                10. Do NOT include any markdown code blocks (no ``` or ~~~)
                11. Do NOT include any comments (no // or /* */)
                12. Do NOT include ANY explanation, introduction, or description text
                13. Use double quotes for all JSON strings, never single quotes
                14. Ensure all JSON braces and brackets are properly closed
                15. If a value is missing or cannot be determined, omit the field (don't include it) or use null
                
                Transform the source data according to the target schema and mapping rules.
                Return ONLY: {the JSON object}""",
                request.getSourceSchema(),
                request.getTargetSchema(),
                targetSampleSection,
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
            
            int firstBrace = jsonString.indexOf('{');
            if (firstBrace > 0) {
                log.debug("Found explanation text before JSON, extracting JSON object");
                jsonString = extractJsonFromResponse(jsonString, firstBrace);
            }
            
            jsonString = jsonString.trim();
            log.debug("Cleaned JSON for parsing: {}", jsonString);

            Map<String, Object> parsedData = objectMapper.readValue(jsonString, Map.class);
            Map<String, Object> fixedData = fixStringifiedNestedObjects(parsedData);
            return fixedData;
        } catch (Exception e) {
            log.error("Failed to parse AI response as JSON: {}", e.getMessage());
            log.debug("AI Response that failed parsing: {}", aiResponse);
            
            throw new RuntimeException("AI mapping returned invalid JSON: " + e.getMessage(), e);
        }
    }
    
    private String extractJsonFromResponse(String response, int startIndex) {
        int braceCount = 0;
        int startPos = -1;
        
        for (int i = startIndex; i < response.length(); i++) {
            char c = response.charAt(i);
            
            if (c == '{') {
                if (startPos == -1) {
                    startPos = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && startPos != -1) {
                    return response.substring(startPos, i + 1);
                }
            }
        }
        
        return response.substring(startIndex);
    }
    
    private Map<String, Object> fixStringifiedNestedObjects(Map<String, Object> data) {
        Map<String, Object> result = new LinkedHashMap<>(data);
        
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            Object value = entry.getValue();
            
            if (value instanceof String) {
                String strValue = (String) value;
                if ((strValue.startsWith("{") && strValue.endsWith("}")) ||
                    (strValue.startsWith("[") && strValue.endsWith("]"))) {
                    try {
                        Object parsedValue = objectMapper.readValue(strValue, Object.class);
                        log.debug("Converted stringified nested structure for field {}: {} -> {}", 
                                entry.getKey(), strValue, parsedValue);
                        result.put(entry.getKey(), parsedValue);
                    } catch (Exception e) {
                        log.debug("Could not parse stringified value for field {}: {}", 
                                entry.getKey(), strValue);
                    }
                }
            } else if (value instanceof Map) {
                result.put(entry.getKey(), fixStringifiedNestedObjects((Map<String, Object>) value));
            }
        }
        
        return result;
    }

    public String generateMappingRules(String sourceSchema, String targetSchema) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Generating AI mapping rules - source: {}, target: {}", sourceSchema, targetSchema);

            String prompt = buildMappingRulesPrompt(sourceSchema, targetSchema);
            String aiResponse = callOllamaApi(prompt);

            log.info("Generated mapping rules: {}", aiResponse);

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Mapping rules generation completed in {}ms", executionTime);

            return aiResponse;

        } catch (Exception e) {
            log.error("Error generating mapping rules", e);
            throw new RuntimeException("Failed to generate mapping rules: " + e.getMessage(), e);
        }
    }

    private String buildMappingRulesPrompt(String sourceSchema, String targetSchema) {
        return String.format(
                """
                You are a data mapping expert. Analyze the source and target schemas, and generate mapping rules.
                
                SOURCE SCHEMA:
                %s
                
                TARGET SCHEMA:
                %s
                
                Generate mapping rules in the following format (one rule per line):
                - Map [source_field] to [target_field]
                
                IMPORTANT RULES:
                1. Only include fields that have EXACT NAME MATCHES between source and target
                2. Do NOT map fields with similar but different names (e.g., 'source_amount' to 'amount' is NOT allowed)
                3. For fields in TARGET that don't have exact source matches, DO NOT include them in the rules
                4. These unmapped target fields will be identified separately for manual configuration
                5. Only output the mapping rules, nothing else - no explanations or comments""",
                sourceSchema,
                targetSchema
        );
    }
}
