package com.example.integrationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MappingRequestDto {
    @JsonProperty("source_data")
    private Map<String, Object> sourceData;

    @JsonProperty("source_schema")
    private String sourceSchema;

    @JsonProperty("target_schema")
    private String targetSchema;

    @JsonProperty("mapping_rules")
    private String mappingRules;
}
