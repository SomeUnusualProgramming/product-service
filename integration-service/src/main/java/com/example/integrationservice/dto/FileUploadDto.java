package com.example.integrationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDto {
    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("file_type")
    private String fileType;

    @JsonProperty("detected_schema")
    private Map<String, String> detectedSchema;

    @JsonProperty("sample_rows")
    private List<Map<String, Object>> sampleRows;

    @JsonProperty("total_rows")
    private Integer totalRows;

    @JsonProperty("mapping_rules")
    private String mappingRules;

    @JsonProperty("target_schema")
    private String targetSchema;
}
