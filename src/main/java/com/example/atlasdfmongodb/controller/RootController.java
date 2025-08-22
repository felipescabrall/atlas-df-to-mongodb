package com.example.atlasdfmongodb.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class RootController {

    /**
     * Endpoint raiz da API
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("application", "Atlas DataFrame to MongoDB");
        response.put("version", "1.0.0");
        response.put("description", "API para processamento e transferência de dados entre MongoDB Atlas");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "running");
        
        // Endpoints disponíveis
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /", "Informações da API");
        endpoints.put("GET /health", "Health check básico");
        endpoints.put("GET /health/detailed", "Health check detalhado");
        endpoints.put("GET /health/mongodb", "Health check MongoDB");
        endpoints.put("GET /fluxo/status", "Status geral do sistema");
        endpoints.put("GET /fluxo/processos", "Lista todos os processos");
        endpoints.put("GET /fluxo/processos/ativos", "Lista processos ativos");
        endpoints.put("GET /fluxo/processos/{id}", "Detalhes de um processo específico");
        endpoints.put("POST /fluxo/executar", "Executa o fluxo principal manualmente");
        
        response.put("endpoints", endpoints);
        
        // Informações técnicas
        Map<String, Object> technical = new HashMap<>();
        technical.put("java_version", System.getProperty("java.version"));
        technical.put("spring_boot", "3.2.5");
        technical.put("mongodb_driver", "Spring Data MongoDB");
        technical.put("context_path", "/api");
        
        response.put("technical_info", technical);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para informações da API
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("name", "Atlas DataFrame to MongoDB");
        response.put("version", "1.0.0");
        response.put("description", "Sistema de processamento e transferência de dados entre instâncias MongoDB Atlas");
        response.put("author", "Sistema Bradesco");
        response.put("build_time", LocalDateTime.now());
        
        // Funcionalidades
        Map<String, String> features = new HashMap<>();
        features.put("data_processing", "Processamento automatizado de dados");
        features.put("mongodb_integration", "Integração com múltiplas instâncias MongoDB");
        features.put("scheduling", "Execução agendada de fluxos");
        features.put("monitoring", "Monitoramento e controle de processos");
        features.put("error_handling", "Tratamento e log de erros");
        features.put("health_checks", "Verificações de saúde do sistema");
        
        response.put("features", features);
        
        // Configurações
        Map<String, Object> config = new HashMap<>();
        config.put("server_port", "8080");
        config.put("context_path", "/api");
        config.put("mongodb_databases", new String[]{"bradesco", "bradesco_flat"});
        config.put("scheduling_enabled", true);
        config.put("cors_enabled", true);
        
        response.put("configuration", config);
        
        return ResponseEntity.ok(response);
    }
}