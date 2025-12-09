package com.example.integrationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingResponseDto {
    @JsonProperty("mapping_id")
    private String mappingId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("mapped_data")
    private Map<String, Object> mappedData;

    @JsonProperty("transformation_details")
    private String transformationDetails;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("processed_at")
    private LocalDateTime processedAt;

    @JsonProperty("execution_time_ms")
    private Long executionTimeMs;
}
