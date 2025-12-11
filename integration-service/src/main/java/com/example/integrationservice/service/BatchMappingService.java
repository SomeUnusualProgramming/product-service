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
            String mappingRules
    ) {
        long startTime = System.currentTimeMillis();
        String batchId = UUID.randomUUID().toString();
        List<Map<String, Object>> mappedResults = new ArrayList<>();
        List<Map<String, String>> errors = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        log.info("Starting batch mapping for file: {} with {} rows", fileName, rows.size());

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);

            try {
                MappingRequestDto request = MappingRequestDto.builder()
                        .sourceData(row)
                        .sourceSchema(objectMapper.writeValueAsString(sourceSchema))
                        .targetSchema(targetSchema)
                        .mappingRules(mappingRules)
                        .build();

                MappingResponseDto response = aiMappingService.mapData(request);

                if ("SUCCESS".equals(response.getStatus())) {
                    mappedResults.add(response.getMappedData());
                    successCount.incrementAndGet();
                } else {
                    failureCount.incrementAndGet();
                    errors.add(Map.of(
                            "row_index", String.valueOf(i),
                            "error", response.getErrorMessage() != null ? response.getErrorMessage() : "Unknown error"
                    ));
                }

            } catch (Exception e) {
                failureCount.incrementAndGet();
                errors.add(Map.of(
                        "row_index", String.valueOf(i),
                        "error", e.getMessage()
                ));
                log.error("Error mapping row {}", i, e);
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;

        log.info("Batch mapping completed: {} successful, {} failed, took {}ms",
                successCount.get(), failureCount.get(), processingTime);

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
}
