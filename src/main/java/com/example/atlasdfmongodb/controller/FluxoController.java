package com.example.atlasdfmongodb.controller;

import com.example.atlasdfmongodb.model.ProcessControl;
import com.example.atlasdfmongodb.model.ProcessLog;
import com.example.atlasdfmongodb.repository.ProcessControlRepository;
import com.example.atlasdfmongodb.service.FluxoPrincipalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/fluxo")
@CrossOrigin(origins = "*")
public class FluxoController {

    private static final Logger logger = LoggerFactory.getLogger(FluxoController.class);

    @Autowired
    private FluxoPrincipalService fluxoPrincipalService;
    
    @Autowired
    private ProcessControlRepository processControlRepository;

    /**
     * Executa o fluxo principal manualmente
     */
    @PostMapping("/executar")
    public ResponseEntity<ProcessControl> executarFluxo() {
        try {
            logger.info("Solicitação de execução manual do fluxo principal recebida");
            ProcessControl resultado = fluxoPrincipalService.executarManualmente();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            logger.error("Erro durante execução manual do fluxo", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtém estatísticas do processamento
     */
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        try {
            Map<String, Object> stats = fluxoPrincipalService.obterEstatisticas();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtém logs de um processo específico
     */
    @GetMapping("/logs/{processoId}")
    public ResponseEntity<List<ProcessLog>> obterLogsProcesso(@PathVariable String processoId) {
        try {
            List<ProcessLog> logs = fluxoPrincipalService.obterLogsProcesso(processoId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Erro ao obter logs do processo: {}", processoId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtém todos os logs (últimos 100)
     */
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> obterTodosLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        try {
            // Implementar paginação se necessário
            Map<String, Object> response = Map.of(
                "message", "Use /logs/{processoId} para obter logs de um processo específico",
                "endpoints", Map.of(
                    "logs_por_processo", "/api/fluxo/logs/{processoId}",
                    "status_controle", "/api/fluxo/status",
                    "estatisticas", "/api/fluxo/estatisticas"
                )
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao obter logs", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtém status atual do controle de processo
     */
    @GetMapping("/status")
    public ResponseEntity<ProcessControl> obterStatusControle() {
        try {
            Optional<ProcessControl> controle = fluxoPrincipalService.obterStatusControle();
            return controle.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Erro ao obter status do controle", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lista todos os processos
     */
    @GetMapping("/processos")
    public ResponseEntity<List<ProcessControl>> listarProcessos() {
        try {
            logger.info("Solicitação para listar todos os processos recebida");
            List<ProcessControl> processos = processControlRepository.findAll();
            return ResponseEntity.ok(processos);
        } catch (Exception e) {
            logger.error("Erro ao listar processos", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Lista processos ativos (em execução)
     */
    @GetMapping("/processos/ativos")
    public ResponseEntity<List<ProcessControl>> listarProcessosAtivos() {
        try {
            logger.info("Solicitação para listar processos ativos recebida");
            List<ProcessControl> processosAtivos = processControlRepository.findAllByStatus(ProcessControl.StatusProcesso.EM_EXECUCAO);
            return ResponseEntity.ok(processosAtivos);
        } catch (Exception e) {
            logger.error("Erro ao listar processos ativos", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reset do controle de processo para permitir nova execução
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetarControleProcesso() {
        try {
            logger.info("Solicitação de reset do controle de processo recebida");
            
            Optional<ProcessControl> controleOpt = processControlRepository.findControlDocument();
            Map<String, Object> response = new HashMap<>();
            
            if (controleOpt.isPresent()) {
                ProcessControl controle = controleOpt.get();
                ProcessControl.StatusProcesso statusAnterior = controle.getStatus();
                
                controle.setStatus(ProcessControl.StatusProcesso.PRONTO);
                controle.setDataAtualizacao(LocalDateTime.now());
                controle.setErroDetalhes(null);
                processControlRepository.save(controle);
                
                response.put("sucesso", true);
                response.put("statusAnterior", statusAnterior);
                response.put("statusAtual", ProcessControl.StatusProcesso.PRONTO);
                response.put("mensagem", "Controle de processo resetado com sucesso");
                
                logger.info("Controle de processo resetado. Status anterior: {}, Status atual: PRONTO", statusAnterior);
            } else {
                response.put("sucesso", true);
                response.put("mensagem", "Nenhum controle de processo encontrado");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erro ao resetar controle de processo", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("sucesso", false);
            errorResponse.put("erro", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Obtém detalhes de um processo específico
     */
    @GetMapping("/processos/{id}")
    public ResponseEntity<ProcessControl> obterProcesso(@PathVariable String id) {
        try {
            logger.info("Solicitação para obter detalhes do processo: {}", id);
            Optional<ProcessControl> processo = processControlRepository.findByProcessoId(id);
            return processo.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Erro ao obter detalhes do processo: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}