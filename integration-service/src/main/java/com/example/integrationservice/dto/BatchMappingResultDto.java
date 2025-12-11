package com.example.integrationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchMappingResultDto {
    @JsonProperty("batch_id")
    private String batchId;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("total_rows_processed")
    private Integer totalRowsProcessed;

    @JsonProperty("successful_mappings")
    private Integer successfulMappings;

    @JsonProperty("failed_mappings")
    private Integer failedMappings;

    @JsonProperty("mapped_data")
    private List<Map<String, Object>> mappedData;

    @JsonProperty("errors")
    private List<Map<String, String>> errors;

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;

    @JsonProperty("processed_at")
    private LocalDateTime processedAt;

    @JsonProperty("download_url")
    private String downloadUrl;
}
