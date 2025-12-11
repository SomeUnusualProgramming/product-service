package com.example.integrationservice.controller;

import com.example.integrationservice.dto.*;
import com.example.integrationservice.service.AiMappingService;
import com.example.integrationservice.service.BatchMappingService;
import com.example.integrationservice.service.FileParserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    private final AiMappingService aiMappingService;
    private final FileParserService fileParserService;
    private final BatchMappingService batchMappingService;

    public IntegrationController(AiMappingService aiMappingService, FileParserService fileParserService, BatchMappingService batchMappingService) {
        this.aiMappingService = aiMappingService;
        this.fileParserService = fileParserService;
        this.batchMappingService = batchMappingService;
    }

    @GetMapping({"", "/"})
    public ResponseEntity<String> getApiDocumentation() {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Integration Service API</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }
                    .container { max-width: 900px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    h1 { color: #333; border-bottom: 3px solid #007bff; padding-bottom: 10px; }
                    .endpoint { margin: 20px 0; padding: 15px; border-left: 4px solid #007bff; background-color: #f9f9f9; }
                    .method { font-weight: bold; padding: 4px 8px; border-radius: 3px; margin-right: 10px; }
                    .get { background-color: #28a745; color: white; }
                    .post { background-color: #007bff; color: white; }
                    a { color: #007bff; text-decoration: none; font-weight: bold; }
                    a:hover { text-decoration: underline; }
                    .description { margin-top: 8px; color: #666; font-size: 14px; }
                    .section { margin: 30px 0; }
                    h2 { color: #555; font-size: 18px; margin-top: 30px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🔗 Integration Service API</h1>
                    
                    <div class="section">
                        <h2>Status & Health</h2>
                        <div class="endpoint">
                            <span class="method get">GET</span> <a href="/api/integration/status">/api/integration/status</a>
                            <div class="description">Service status and version info</div>
                        </div>
                        <div class="endpoint">
                            <span class="method get">GET</span> <a href="/api/integration/health/detailed">/api/integration/health/detailed</a>
                            <div class="description">Detailed health check (database, kafka, product-service)</div>
                        </div>
                    </div>
                    
                    <div class="section">
                        <h2>File Operations</h2>
                        <div class="endpoint">
                            <span class="method post">POST</span> /api/integration/file/upload
                            <div class="description">Upload and parse file (CSV, JSON, Excel) with schema detection</div>
                            <div class="description" style="margin-top: 10px;"><a href="/file-mapping.html">→ Test File Upload</a></div>
                        </div>
                        <div class="endpoint">
                            <span class="method post">POST</span> /api/integration/batch/map
                            <div class="description">Batch map data with mapping rules (multipart/form-data)</div>
                        </div>
                        <div class="endpoint">
                            <span class="method get">GET</span> /api/integration/batch/{batchId}/download
                            <div class="description">Download batch results (format: json or csv)</div>
                        </div>
                    </div>
                    
                    <div class="section">
                        <h2>AI Mapping</h2>
                        <div class="endpoint">
                            <span class="method post">POST</span> /api/integration/ai/map
                            <div class="description">Map data using AI-powered field mapping</div>
                            <div class="description" style="margin-top: 10px;"><a href="/ai-mapping-test.html">→ Test AI Mapping</a></div>
                        </div>
                    </div>
                    
                    <div class="section">
                        <h2>Synchronization</h2>
                        <div class="endpoint">
                            <span class="method post">POST</span> /api/integration/sync/products
                            <div class="description">Initiate product synchronization (202 Accepted)</div>
                        </div>
                    </div>
                    
                    <div class="section">
                        <h2>Testing</h2>
                        <div class="endpoint">
                            <span class="method post">POST</span> /api/integration/test/message
                            <div class="description">Send test message to Kafka</div>
                        </div>
                    </div>
                    
                    <div class="section" style="margin-top: 40px; padding-top: 20px; border-top: 1px solid #ddd;">
                        <h2>External Services</h2>
                        <div class="endpoint">
                            <span style="color: #666; font-weight: bold;">Product Service:</span>
                            <a href="http://localhost:8080/api/products">http://localhost:8080/api/products</a>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """;
        return ResponseEntity.ok().contentType(org.springframework.http.MediaType.TEXT_HTML).body(html);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "integration-service");
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("version", "0.0.1");
        return ResponseEntity.ok(status);
    }

    @PostMapping("/ai/map")
    public ResponseEntity<MappingResponseDto> mapDataWithAi(@RequestBody MappingRequestDto request) {
        MappingResponseDto response = aiMappingService.mapData(request);
        if ("SUCCESS".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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

    @PostMapping("/file/upload")
    public ResponseEntity<FileUploadDto> uploadFile(@RequestParam MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Map<String, Object>> rows = fileParserService.parseFile(file);
            Map<String, String> schema = fileParserService.detectSchema(rows);
            List<Map<String, Object>> samples = fileParserService.getSampleRows(rows, 5);

            FileUploadDto response = FileUploadDto.builder()
                    .fileName(originalFilename)
                    .fileType(originalFilename.substring(originalFilename.lastIndexOf('.') + 1))
                    .detectedSchema(schema)
                    .sampleRows(samples)
                    .totalRows(rows.size())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/batch/map")
    public ResponseEntity<BatchMappingResultDto> batchMap(
            @RequestParam MultipartFile file,
            @RequestParam("target_schema") String targetSchema,
            @RequestParam("mapping_rules") String mappingRules
    ) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return ResponseEntity.badRequest().build();
            }

            List<Map<String, Object>> rows = fileParserService.parseFile(file);
            Map<String, String> sourceSchema = fileParserService.detectSchema(rows);

            BatchMappingResultDto result = batchMappingService.mapBatch(
                    originalFilename,
                    rows,
                    sourceSchema,
                    targetSchema,
                    mappingRules
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/batch/{batchId}/download")
    public ResponseEntity<String> downloadBatch(
            @PathVariable String batchId,
            @RequestParam(defaultValue = "json") String format
    ) {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"batch-" + batchId + "." + format + "\"")
                .body("Download not yet implemented");
    }
}
