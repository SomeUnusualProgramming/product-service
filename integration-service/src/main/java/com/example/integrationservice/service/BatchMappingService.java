package com.example.integrationservice.service;

import com.example.integrationservice.dto.BatchMappingResultDto;
import com.example.integrationservice.dto.MappingRequestDto;
import com.example.integrationservice.dto.MappingResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class BatchMappingService {
    private final AiMappingService aiMappingService;
    private final ObjectMapper objectMapper;

    public BatchMappingService(AiMappingService aiMappingService, ObjectMapper objectMapper) {
        this.aiMappingService = aiMappingService;
        this.objectMapper = objectMapper;
    }

    public BatchMappingResultDto mapBatch(
            String fileName,
            List<Map<String, Object>> rows,
            Map<String, String> sourceSchema,
            String targetSchema,
            String mappingRules,
            Map<String, Object> targetSampleData
    ) {
        long startTime = System.currentTimeMillis();
        String batchId = UUID.randomUUID().toString();
        List<Map<String, Object>> mappedResults = new ArrayList<>();
        List<Map<String, String>> errors = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        Map<String, String> targetSchemaMap = parseTargetSchema(targetSchema);
        log.info("Starting batch mapping for file: {} with {} rows. Target schema fields: {}", 
                fileName, rows.size(), targetSchemaMap.keySet());

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            log.debug("Processing row {} of {}: {}", i + 1, rows.size(), row);

            try {
                String targetSampleJson = targetSampleData != null ? 
                    objectMapper.writeValueAsString(targetSampleData) : "";
                    
                MappingRequestDto request = MappingRequestDto.builder()
                        .sourceData(row)
                        .sourceSchema(objectMapper.writeValueAsString(sourceSchema))
                        .targetSchema(targetSchema)
                        .mappingRules(mappingRules)
                        .targetSampleData(targetSampleJson)
                        .build();

                MappingResponseDto response = aiMappingService.mapData(request);

                if ("SUCCESS".equals(response.getStatus())) {
                    Map<String, Object> mappedData = response.getMappedData();
                    
                    if (mappedData != null && !mappedData.isEmpty()) {
                        if (!mappedData.containsKey("parse_error") && !mappedData.containsKey("raw_response")) {
                            Map<String, Object> cleanedData = cleanNullValues(mappedData);
                            Map<String, Object> filteredData = filterByTargetSchema(cleanedData, targetSchemaMap);
                            mappedResults.add(filteredData);
                            successCount.incrementAndGet();
                            log.debug("Row {} mapped successfully. Total successful: {}", i + 1, successCount.get());
                        } else {
                            failureCount.incrementAndGet();
                            String errorMsg = mappedData.get("parse_error") != null ? 
                                mappedData.get("parse_error").toString() : "JSON parsing failed";
                            errors.add(Map.of(
                                    "row_index", String.valueOf(i),
                                    "error", errorMsg,
                                    "type", "json_parse_error"
                            ));
                            log.warn("Row {} JSON parsing failed: {}", i + 1, errorMsg);
                        }
                    } else {
                        failureCount.incrementAndGet();
                        errors.add(Map.of(
                                "row_index", String.valueOf(i),
                                "error", "Mapped data is null or empty"
                        ));
                        log.warn("Row {} mapping returned null/empty data", i + 1);
                    }
                } else {
                    failureCount.incrementAndGet();
                    String errorMsg = response.getErrorMessage() != null ? response.getErrorMessage() : "Unknown error";
                    errors.add(Map.of(
                            "row_index", String.valueOf(i),
                            "error", errorMsg,
                            "type", "mapping_error"
                    ));
                    log.warn("Row {} mapping failed: {}", i + 1, errorMsg);
                }

            } catch (Exception e) {
                failureCount.incrementAndGet();
                errors.add(Map.of(
                        "row_index", String.valueOf(i),
                        "error", e.getMessage()
                ));
                log.error("Exception mapping row {}: {}", i + 1, e.getMessage(), e);
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;

        log.info("Batch mapping completed: {} successful, {} failed, took {}ms. Final mappedResults size: {}",
                successCount.get(), failureCount.get(), processingTime, mappedResults.size());

        return BatchMappingResultDto.builder()
                .batchId(batchId)
                .fileName(fileName)
                .totalRowsProcessed(rows.size())
                .successfulMappings(successCount.get())
                .failedMappings(failureCount.get())
                .mappedData(mappedResults)
                .errors(errors.isEmpty() ? null : errors)
                .processingTimeMs(processingTime)
                .processedAt(LocalDateTime.now())
                .downloadUrl("/api/integration/batch/" + batchId + "/download")
                .build();
    }

    public String exportToCSV(List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            return "";
        }

        StringBuilder csv = new StringBuilder();
        Map<String, Object> firstRow = data.get(0);

        csv.append(String.join(",", firstRow.keySet())).append("\n");

        for (Map<String, Object> row : data) {
            List<String> values = new ArrayList<>();
            for (Object value : row.values()) {
                String escaped = value != null ? value.toString().replace("\"", "\"\"") : "";
                values.add("\"" + escaped + "\"");
            }
            csv.append(String.join(",", values)).append("\n");
        }

        return csv.toString();
    }

    public String exportToJSON(List<Map<String, Object>> data) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
    }

    private Map<String, Object> cleanNullValues(Map<String, Object> data) {
        Map<String, Object> cleaned = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                cleaned.put(entry.getKey(), entry.getValue());
            }
        }
        return cleaned;
    }

    private Map<String, String> parseTargetSchema(String targetSchemaJson) {
        try {
            return objectMapper.readValue(targetSchemaJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse target schema: {}", e.getMessage());
            return new java.util.HashMap<>();
        }
    }

    private Map<String, Object> filterByTargetSchema(Map<String, Object> data, Map<String, String> targetSchema) {
        if (targetSchema.isEmpty()) {
            return data;
        }
        
        Map<String, Object> filtered = new java.util.LinkedHashMap<>();
        for (String field : targetSchema.keySet()) {
            if (data.containsKey(field)) {
                filtered.put(field, data.get(field));
            }
        }
        
        log.debug("Filtered data from {} fields to {} fields", data.size(), filtered.size());
        return filtered;
    }
}
