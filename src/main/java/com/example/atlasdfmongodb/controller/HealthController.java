package com.example.atlasdfmongodb.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@CrossOrigin(origins = "*")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
@Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate primaryMongoTemplate;

    @Autowired
    @Qualifier("flatMongoTemplate")
    private MongoTemplate flatMongoTemplate;

    @Autowired
    @Qualifier("controlMongoTemplate")
    private MongoTemplate controlMongoTemplate;

    @Value("${app.mongodb.collection.health-check}")
    private String collectionHealthCheck;

    /**
     * Health check básico da aplicação
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        boolean isHealthy = true;
        
        try {
            // Verificar conexão com banco principal
            long countPrincipal = primaryMongoTemplate.estimatedCount("flat");
            health.put("mongodb_principal", Map.of(
                "status", "UP",
                "database", "bradesco",
                "collection", "flat",
                "estimated_count", countPrincipal
            ));
            
        } catch (Exception e) {
            logger.error("Erro ao verificar saúde do banco principal", e);
            health.put("mongodb_principal", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
            isHealthy = false;
        }
        
        try {
            // Verificar conexão com banco flat
            long countFlat = flatMongoTemplate.estimatedCount("temp");
            health.put("mongodb_flat", Map.of(
                "status", "UP",
                "database", "bradesco_flat",
                "collection", "temp",
                "estimated_count", countFlat
            ));
            
        } catch (Exception e) {
            logger.error("Erro ao verificar saúde do banco flat", e);
            health.put("mongodb_flat", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
            isHealthy = false;
        }
        
        try {
            // Verificar conexão com banco de controle
            long countControl = controlMongoTemplate.estimatedCount("process_control");
            health.put("mongodb_control", Map.of(
                "status", "UP",
                "database", "felipescabral",
                "collection", "process_control",
                "estimated_count", countControl
            ));
            
        } catch (Exception e) {
            logger.error("Erro ao verificar saúde do banco de controle", e);
            health.put("mongodb_control", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
            isHealthy = false;
        }
        
        health.put("overall_status", isHealthy ? "UP" : "DOWN");
        health.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(health);
    }

    /**
     * Health check detalhado incluindo conectividade com MongoDB
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> checks = new HashMap<>();
        boolean allHealthy = true;
        
        try {
            // Check MongoDB Principal
            try {
                primaryMongoTemplate.getCollection(collectionHealthCheck).estimatedDocumentCount();
                checks.put("mongodb_primary", createHealthStatus("UP", "Conexão com MongoDB principal OK"));
            } catch (Exception e) {
                checks.put("mongodb_primary", createHealthStatus("DOWN", "Erro na conexão com MongoDB principal: " + e.getMessage()));
                allHealthy = false;
            }

            // Check MongoDB Flat
            try {
                flatMongoTemplate.getCollection(collectionHealthCheck).estimatedDocumentCount();
                checks.put("mongodb_flat", createHealthStatus("UP", "Conexão com MongoDB flat OK"));
            } catch (Exception e) {
                checks.put("mongodb_flat", createHealthStatus("DOWN", "Erro na conexão com MongoDB flat: " + e.getMessage()));
                allHealthy = false;
            }

            // Check Memória
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
            
            Map<String, Object> memoryInfo = new HashMap<>();
            memoryInfo.put("max_memory_mb", maxMemory / 1024 / 1024);
            memoryInfo.put("total_memory_mb", totalMemory / 1024 / 1024);
            memoryInfo.put("used_memory_mb", usedMemory / 1024 / 1024);
            memoryInfo.put("free_memory_mb", freeMemory / 1024 / 1024);
            memoryInfo.put("usage_percent", Math.round(memoryUsagePercent * 100.0) / 100.0);
            
            String memoryStatus = memoryUsagePercent > 90 ? "WARNING" : "UP";
            String memoryMessage = memoryUsagePercent > 90 ? 
                "Uso de memória alto: " + Math.round(memoryUsagePercent) + "%" : 
                "Uso de memória normal: " + Math.round(memoryUsagePercent) + "%";
            
            checks.put("memory", createHealthStatus(memoryStatus, memoryMessage, memoryInfo));
            
            if (memoryUsagePercent > 95) {
                allHealthy = false;
            }

            // Status geral
            response.put("status", allHealthy ? "UP" : "DOWN");
            response.put("timestamp", LocalDateTime.now());
            response.put("application", "Atlas DataFrame to MongoDB");
            response.put("version", "1.0.0");
            response.put("checks", checks);
            
            HttpStatus httpStatus = allHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            return ResponseEntity.status(httpStatus).body(response);
            
        } catch (Exception e) {
            logger.error("Erro durante health check detalhado", e);
            
            response.put("status", "DOWN");
            response.put("timestamp", LocalDateTime.now());
            response.put("error", e.getMessage());
            response.put("checks", checks);
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    /**
     * Endpoint para verificar conectividade específica com MongoDB
     */
    @GetMapping("/mongodb")
    public ResponseEntity<Map<String, Object>> mongoHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test primary MongoDB
            long primaryCount = primaryMongoTemplate.getCollection(collectionHealthCheck).estimatedDocumentCount();
            
            // Test flat MongoDB
            long flatCount = flatMongoTemplate.getCollection(collectionHealthCheck).estimatedDocumentCount();
            
            response.put("status", "UP");
            response.put("timestamp", LocalDateTime.now());
            response.put("primary_mongodb", "Connected");
            response.put("flat_mongodb", "Connected");
            response.put("primary_collections_checked", primaryCount >= 0);
            response.put("flat_collections_checked", flatCount >= 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Erro durante MongoDB health check", e);
            
            response.put("status", "DOWN");
            response.put("timestamp", LocalDateTime.now());
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }

    /**
     * Cria um objeto de status de saúde padronizado
     */
    private Map<String, Object> createHealthStatus(String status, String message) {
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", status);
        healthStatus.put("message", message);
        healthStatus.put("timestamp", LocalDateTime.now());
        return healthStatus;
    }

    /**
     * Cria um objeto de status de saúde padronizado com detalhes adicionais
     */
    private Map<String, Object> createHealthStatus(String status, String message, Object details) {
        Map<String, Object> healthStatus = createHealthStatus(status, message);
        healthStatus.put("details", details);
        return healthStatus;
    }
}