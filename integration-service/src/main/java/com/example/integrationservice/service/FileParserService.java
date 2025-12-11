package com.example.integrationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class FileParserService {
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    public FileParserService(ObjectMapper objectMapper, XmlMapper xmlMapper) {
        this.objectMapper = objectMapper;
        this.xmlMapper = xmlMapper;
    }

    public List<Map<String, Object>> parseFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Invalid file");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty: " + filename);
        }

        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".csv")) {
            return parseCSV(file);
        } else if (lower.endsWith(".json")) {
            return parseJSON(file);
        } else if (lower.endsWith(".xml")) {
            return parseXML(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filename);
        }
    }

    private List<Map<String, Object>> parseCSV(MultipartFile file) throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (InputStream in = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                Map<String, Object> row = new LinkedHashMap<>(record.toMap());
                rows.add(row);
            }
        }

        return rows;
    }

    private List<Map<String, Object>> parseJSON(MultipartFile file) throws Exception {
        try (InputStream in = file.getInputStream()) {
            Object parsed = objectMapper.readValue(in, Object.class);

            if (parsed instanceof List) {
                //noinspection unchecked
                return (List<Map<String, Object>>) parsed;
            } else if (parsed instanceof Map) {
                //noinspection unchecked
                return List.of((Map<String, Object>) parsed);
            } else {
                throw new IllegalArgumentException("JSON must be an array or object");
            }
        }
    }

    private List<Map<String, Object>> parseXML(MultipartFile file) throws Exception {
        try (InputStream in = file.getInputStream()) {
            Map<String, Object> parsed = xmlMapper.readValue(in, Map.class);
            return List.of(parsed);
        }
    }

    public Map<String, String> detectSchema(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> schema = new LinkedHashMap<>();
        Map<String, Object> firstRow = rows.get(0);

        for (Map.Entry<String, Object> entry : firstRow.entrySet()) {
            Object value = entry.getValue();
            String type = detectType(value);
            schema.put(entry.getKey(), type);
        }

        return schema;
    }

    private String detectType(Object value) {
        if (value == null) {
            return "String";
        }

        if (value instanceof Number) {
            if (value instanceof Integer || value instanceof Long) {
                return "Integer";
            }
            return "BigDecimal";
        } else if (value instanceof Boolean) {
            return "Boolean";
        } else if (value instanceof String str) {
            if (str.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                return "LocalDateTime";
            }
            if (str.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                return "UUID";
            }
            return "String";
        } else if (value instanceof List) {
            return "List";
        } else if (value instanceof Map) {
            return "Map";
        }

        return "String";
    }

    public List<Map<String, Object>> getSampleRows(List<Map<String, Object>> rows, int sampleSize) {
        if (rows == null) return Collections.emptyList();
        int size = Math.min(sampleSize, rows.size());
        return rows.subList(0, size);
    }
}
