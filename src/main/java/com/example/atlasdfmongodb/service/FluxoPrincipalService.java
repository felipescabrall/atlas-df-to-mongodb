package com.example.atlasdfmongodb.service;

import com.example.atlasdfmongodb.model.ProcessControl;
import com.example.atlasdfmongodb.model.ProcessLog;
import com.example.atlasdfmongodb.repository.ProcessControlRepository;
import com.example.atlasdfmongodb.repository.ProcessLogRepository;
import com.example.atlasdfmongodb.repository.LoadDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.mongodb.client.model.IndexOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Service
public class FluxoPrincipalService {

    private static final Logger logger = LoggerFactory.getLogger(FluxoPrincipalService.class);

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate primaryMongoTemplate;

    @Autowired
    @Qualifier("flatMongoTemplate")
    private MongoTemplate flatMongoTemplate;

    @Autowired
    @Qualifier("controlMongoTemplate")
    private MongoTemplate controlMongoTemplate;

    @Autowired
    private ProcessControlRepository processControlRepository;

    @Autowired
    private ProcessLogRepository processLogRepository;

    @Autowired
    private LoadDataRepository loadDataRepository;

    // Configurações das coleções
    @Value("${mongodb.principal.collection.flat}")
    private String collectionFlat;

    @Value("${mongodb.flat.collection.temp}")
    private String collectionTemp;

    @Value("${mongodb.control.collection.loadData}")
    private String collectionLoadData;

    @Value("${mongodb.control.collection.process-control}")
    private String collectionProcessControl;

    @Value("${mongodb.control.collection.process-logs}")
    private String collectionProcessLogs;

    // Configurações Atlas
    @Value("${mongodb.atlas.project-id}")
    private String atlasProjectId;

    @Value("${mongodb.atlas.cluster-name}")
    private String atlasClusterName;

    @Value("${app.batch.size:1000}")
    private int batchSize;

    @Value("${app.bulk.insert.size:30000}")
    private int bulkInsertSize;

    /**
     * Execução agendada do fluxo principal
     */
    @Scheduled(cron = "${app.scheduler.cron}")
    public void executarFluxoPrincipal() {
        String processoId = UUID.randomUUID().toString();
        logger.info("Iniciando fluxo principal - Processo ID: {}", processoId);

        try {
            // Verificar controle de concorrência
            logger.info("Verificando controle de concorrência - Processo ID: {}", processoId);
            if (!verificarEIniciarProcesso(processoId)) {
                logger.warn("Processo já em execução ou com erro. Abortando - Processo ID: {}", processoId);
                return;
            }
            logger.info("Controle de concorrência OK, prosseguindo - Processo ID: {}", processoId);

            // Etapa 1: Validação
            logger.info("Iniciando Etapa 1: Validação - Processo ID: {}", processoId);
            if (!executarEtapa1Validacao(processoId)) {
                logger.info("Etapa 1 falhou: Nenhum dado para processar - Processo ID: {}", processoId);
                finalizarProcessoComStatus(processoId, ProcessControl.StatusProcesso.PRONTO);
                return;
            }
            logger.info("Etapa 1 concluída com sucesso - Processo ID: {}", processoId);

            // Etapa 2: Movimentação de Dados
            logger.info("Iniciando Etapa 2: Movimentação de Dados - Processo ID: {}", processoId);
            executarEtapa2MovimentacaoDados(processoId);
            logger.info("Etapa 2 concluída com sucesso - Processo ID: {}", processoId);

            // Etapa 3: Separação de Dados
            logger.info("Iniciando Etapa 3: Separação de Dados - Processo ID: {}", processoId);
            executarEtapa3SeparacaoDados(processoId);
            logger.info("Etapa 3 concluída com sucesso - Processo ID: {}", processoId);

            // Etapa 4: Limpeza e Finalização
            logger.info("Iniciando Etapa 4: Limpeza e Finalização - Processo ID: {}", processoId);
            executarEtapa4LimpezaFinalizacao(processoId);
            logger.info("Etapa 4 concluída com sucesso - Processo ID: {}", processoId);

            // Finalizar processo com sucesso
            finalizarProcessoComStatus(processoId, ProcessControl.StatusProcesso.PROCESSADO);

            logger.info("Fluxo principal concluído com sucesso - Processo ID: {}", processoId);

        } catch (Exception e) {
            logger.error("Erro durante execução do fluxo principal - Processo ID: {}", processoId, e);
            finalizarProcessoComErro(processoId, e.getMessage());
        }
    }

    /**
     * Execução manual do fluxo principal
     */
    public ProcessControl executarFluxoManual() {
        String processoId = UUID.randomUUID().toString();
        ProcessControl controle = new ProcessControl(ProcessControl.StatusProcesso.PRONTO, processoId);
        
        try {
            logger.info("Iniciando fluxo principal manual - Processo ID: {}", processoId);
            controle.setStatus(ProcessControl.StatusProcesso.EM_EXECUCAO);
            controle.setDataAtualizacao(LocalDateTime.now());
            processControlRepository.save(controle);

            // Etapa 1: Validação
            if (!executarEtapa1Validacao(processoId)) {
                return controle;
            }

            // Este método foi substituído pelo novo fluxo de 4 etapas
            // Manter apenas para compatibilidade
            logger.info("Método executarFluxoManual foi substituído pelo novo fluxo de 4 etapas");

        } catch (Exception e) {
            logger.error("Erro durante execução do fluxo principal - Processo ID: {}", processoId, e);
            controle.setStatus(ProcessControl.StatusProcesso.ERRO);
            controle.setErroDetalhes(e.getMessage());
            controle.setDataAtualizacao(LocalDateTime.now());
        } finally {
            processControlRepository.save(controle);
        }

        return controle;
    }

    /**
     * Verifica controle de concorrência e inicia processo
     */
    private boolean verificarEIniciarProcesso(String processoId) {
        try {
            logger.debug("Buscando documento de controle existente - Processo ID: {}", processoId);
            Optional<ProcessControl> controleExistente = processControlRepository.findControlDocument();
            
            if (controleExistente.isPresent()) {
                ProcessControl controle = controleExistente.get();
                logger.info("Controle existente encontrado - Status: {}, Processo ID anterior: {}", 
                    controle.getStatus(), controle.getProcessoId());
                    
                if (controle.getStatus() == ProcessControl.StatusProcesso.EM_EXECUCAO) {
                    logger.warn("Processo já em execução. Status: {}, Processo ID: {}", 
                        controle.getStatus(), controle.getProcessoId());
                    return false;
                }
                if (controle.getStatus() == ProcessControl.StatusProcesso.ERRO) {
                    logger.warn("Processo anterior terminou com erro. Status: {}, Erro: {}, Processo ID: {}", 
                        controle.getStatus(), controle.getErroDetalhes(), controle.getProcessoId());
                    return false;
                }
                logger.info("Status do controle permite nova execução: {}", controle.getStatus());
            } else {
                logger.info("Nenhum controle existente encontrado, criando novo");
            }
            
            // Criar ou atualizar controle de processo
            ProcessControl novoControle = controleExistente.orElse(new ProcessControl());
            novoControle.setProcessoId(processoId);
            novoControle.setStatus(ProcessControl.StatusProcesso.EM_EXECUCAO);
            novoControle.setDataAtualizacao(LocalDateTime.now());
            logger.info("Salvando novo controle - Status: EM_EXECUCAO, Processo ID: {}", processoId);
            processControlRepository.save(novoControle);
            
            // Log de início
            ProcessLog log = new ProcessLog(processoId, "Processo iniciado", "INICIO", "EM_ANDAMENTO");
            processLogRepository.save(log);
            logger.info("Log de início criado para processo ID: {}", processoId);
            
            return true;
        } catch (Exception e) {
            logger.error("Erro ao verificar controle de processo - Processo ID: {}", processoId, e);
            return false;
        }
    }

    /**
     * Etapa 1: Validação - Verifica se há dados para processar
     */
    private boolean executarEtapa1Validacao(String processoId) {
        ProcessLog log = new ProcessLog(processoId, "Iniciando validação de dados", "VALIDACAO");
        processLogRepository.save(log);
        
        try {
            LocalDate hoje = LocalDate.now();
            String dataAtual = hoje.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            Query query = new Query(Criteria.where("data").is(dataAtual).and("status").is("ready"));
            boolean existemDados = controlMongoTemplate.exists(query, collectionLoadData);
            
            if (!existemDados) {
                log.setMensagem("Nenhum dado disponível para processamento na data atual: " + dataAtual);
                log.finalizarEtapa();
                processLogRepository.save(log);
                return false;
            }
            
            log.setMensagem("Dados disponíveis para processamento encontrados");
            log.finalizarEtapa();
            processLogRepository.save(log);
            return true;
            
        } catch (Exception e) {
             log.finalizarEtapaComErro(e.getMessage());
             processLogRepository.save(log);
             throw new RuntimeException("Erro na etapa de validação", e);
         }
     }

     /**
      * Etapa 2: Movimentação de Dados
      */
     private void executarEtapa2MovimentacaoDados(String processoId) {
         // Mover dados do bradesco.flat para bradesco_flat.temp
         executarEtapa2Parte1MovimentacaoDados(processoId);
     }

     /**
      * Etapa 2: Movimentação de dados usando $out
      */
     private void executarEtapa2Parte1MovimentacaoDados(String processoId) {
         LocalDate hoje = LocalDate.now();
         String dataFormatada = hoje.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
         String collectionTempComData = "temp_" + dataFormatada;
         
         ProcessLog log = new ProcessLog(processoId, "Iniciando processamento e validação de dados com pipeline de agregação", "PROCESSAMENTO_VALIDACAO_DADOS");
         processLogRepository.save(log);
         
         try {
             // Drop da collection temporária se existir
             dropCollectionSeExistir(collectionTempComData, processoId);
             
             // Pipeline de agregação para validação, projeção e saída dos dados
             List<Document> pipeline = Arrays.asList(
                 // Etapa 1: Projeção com validação e estruturação dos dados
                 new Document("$project", new Document()
                     .append("_id", 0)
                     .append("validade", new Document("$cond", new Document()
                         .append("if", new Document("$eq", Arrays.asList(
                             new Document("$strLenCP", "$DATA"), 122)))
                         .append("then", "valido")
                         .append("else", "invalido")))
                     .append("dados", new Document("$cond", new Document()
                         .append("if", new Document("$eq", Arrays.asList(
                             new Document("$strLenCP", "$DATA"), 122)))
                         .append("then", new Document()
                             .append("corp", new Document("$substr", Arrays.asList("$DATA", 0, 2)))
                             .append("cpf", new Document("$substr", Arrays.asList("$DATA", 2, 11)))
                             .append("num_cartao", new Document("$substr", Arrays.asList("$DATA", 13, 16)))
                             .append("bandeira", new Document("$substr", Arrays.asList("$DATA", 29, 1)))
                             .append("desc_produto", new Document("$substr", Arrays.asList("$DATA", 30, 50)))
                             .append("lim_produto", new Document("$substr", Arrays.asList("$DATA", 80, 1)))
                             .append("lim_global", new Document("$substr", Arrays.asList("$DATA", 81, 1)))
                             .append("conta", new Document("$substr", Arrays.asList("$DATA", 82, 16))))
                         .append("else", new Document("original", "$DATA"))))
                     .append("hInclReg", "$$NOW")),
                 
                 // Etapa 2: Saída para collection temporária com data
                 new Document("$out", new Document("atlas", new Document()
                     .append("db", "bradesco_flat")
                     .append("coll", collectionTempComData)
                     .append("projectId", atlasProjectId)
                     .append("clusterName", atlasClusterName)))
             );
             
             // Executar pipeline no banco principal
             primaryMongoTemplate.getCollection(collectionFlat).aggregate(pipeline).toCollection();
             
             // Criar índices na collection temporária
             criarIndicesCollectionTemp(collectionTempComData, processoId);
             
             log.setMensagem("Dados processados, validados e movidos com sucesso para collection: " + collectionTempComData);
             log.finalizarEtapa();
             processLogRepository.save(log);
             
         } catch (Exception e) {
             log.finalizarEtapaComErro(e.getMessage());
             processLogRepository.save(log);
             throw new RuntimeException("Erro no processamento e validação de dados", e);
         }
     }





     /**
      * Etapa 3: Processamento adicional (funcionalidade movida para Etapa 2)
      */
     private void executarEtapa3SeparacaoDados(String processoId) {
         ProcessLog log = new ProcessLog(processoId, "Etapa 3: Processamento adicional - funcionalidade de separação movida para Etapa 2", "ETAPA3_PROCESSAMENTO_ADICIONAL");
         processLogRepository.save(log);
         
         try {
             // A funcionalidade de separação de dados foi movida para executarEtapa2Parte1MovimentacaoDados
             // Esta etapa pode ser usada para processamentos adicionais futuros
             
             LocalDate hoje = LocalDate.now();
             String dataFormatada = hoje.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
             String collectionTempComData = "temp_" + dataFormatada;
             
             // Verificar se a collection temporária existe e tem dados
             long totalDocumentos = flatMongoTemplate.count(new Query(), collectionTempComData);
             
             if (totalDocumentos > 0) {
                 // Contar registros válidos e inválidos
                 Query queryValidos = new Query(Criteria.where("validade").is("valido"));
                 Query queryInvalidos = new Query(Criteria.where("validade").is("invalido"));
                 
                 long totalValidos = flatMongoTemplate.count(queryValidos, collectionTempComData);
                 long totalInvalidos = flatMongoTemplate.count(queryInvalidos, collectionTempComData);
                 
                 log.setMensagem(String.format("Estatísticas da collection %s: Total: %d, Válidos: %d, Inválidos: %d", 
                     collectionTempComData, totalDocumentos, totalValidos, totalInvalidos));
             } else {
                 log.setMensagem("Collection temporária " + collectionTempComData + " não encontrada ou vazia");
             }
             
             log.finalizarEtapa();
             processLogRepository.save(log);
             
         } catch (Exception e) {
             log.finalizarEtapaComErro(e.getMessage());
             processLogRepository.save(log);
             throw new RuntimeException("Erro no processamento adicional da Etapa 3", e);
         }
     }
     
     /**
      * Método auxiliar para extrair substring de forma segura
      */
     private String extrairSubstring(String texto, int inicio, int tamanho) {
         if (texto == null || inicio < 0 || inicio >= texto.length()) {
             return "";
         }
         int fim = Math.min(inicio + tamanho, texto.length());
         return texto.substring(inicio, fim).trim();
     }
     
     /**
      * Remove collection se ela existir
      */
     private void dropCollectionSeExistir(String collectionName, String processoId) {
         ProcessLog log = new ProcessLog(processoId, "Verificando e removendo collection existente: " + collectionName, "DROP_COLLECTION");
         processLogRepository.save(log);
         
         try {
             if (flatMongoTemplate.collectionExists(collectionName)) {
                 flatMongoTemplate.dropCollection(collectionName);
                 log.setMensagem("Collection " + collectionName + " removida com sucesso");
             } else {
                 log.setMensagem("Collection " + collectionName + " não existe, nenhuma ação necessária");
             }
             log.finalizarEtapa();
             processLogRepository.save(log);
             
         } catch (Exception e) {
             log.finalizarEtapaComErro("Erro ao remover collection " + collectionName + ": " + e.getMessage());
             processLogRepository.save(log);
             logger.warn("Erro ao remover collection {}: {}", collectionName, e.getMessage());
             // Não lançar exceção para não interromper o fluxo principal
         }
     }
     
     /**
      * Cria índices na collection temporária
      */
     private void criarIndicesCollectionTemp(String nomeCollection, String processoId) {
         ProcessLog log = new ProcessLog(processoId, "Criando índices na collection: " + nomeCollection, "CRIAR_INDICES_TEMP");
         processLogRepository.save(log);
         
         try {
             // Criar índice no campo validade
             Document validadeIndex = new Document("validade", 1);
             flatMongoTemplate.getCollection(nomeCollection)
                     .createIndex(validadeIndex, new IndexOptions().name("idx_validade"));
             
             
             // Criar índice composto único nos campos de dados válidos (apenas para registros válidos)
             Document dadosIndex = new Document()
                     .append("dados.cpf", 1)
                     .append("dados.num_cartao", 1)
                     .append("dados.corp", 1);
             
             IndexOptions dadosIndexOptions = new IndexOptions()
                     .name("idx_dados_validos_unique")
                     .unique(true)
                     .partialFilterExpression(new Document("validade", "valido"));
             
             flatMongoTemplate.getCollection(nomeCollection)
                     .createIndex(dadosIndex, dadosIndexOptions);
             
             log.setMensagem("Índices criados com sucesso na collection: " + nomeCollection);
             log.finalizarEtapa();
             processLogRepository.save(log);
             
         } catch (Exception e) {
             log.finalizarEtapaComErro(e.getMessage());
             processLogRepository.save(log);
             throw new RuntimeException("Erro ao criar índices na collection: " + nomeCollection, e);
         }
     }

     /**
      * Cria índice único composto na collection de dados válidos
      */
     private void criarIndiceUnicoValidados(String collectionValidos, String processoId) {
         ProcessLog log = new ProcessLog(processoId, "Criando índice único composto (cpf, num_cartao, corp) na collection: " + collectionValidos, "CRIACAO_INDICE_UNICO");
         processLogRepository.save(log);
         
         try {
              // Criar índice composto único com cpf:1, num_cartao:1, corp:1
              Document indexKeys = new Document()
                  .append("cpf", 1)
                  .append("num_cartao", 1)
                  .append("corp", 1);
              
              // Criar o índice na collection
              flatMongoTemplate.getCollection(collectionValidos)
                  .createIndex(indexKeys, new IndexOptions().unique(true));
             
             log.setMensagem("Índice único composto criado com sucesso na collection: " + collectionValidos + " com campos (cpf:1, num_cartao:1, corp:1)");
             log.finalizarEtapa();
             processLogRepository.save(log);
             
         } catch (Exception e) {
             log.finalizarEtapaComErro("Erro ao criar índice único: " + e.getMessage());
             processLogRepository.save(log);
             logger.warn("Erro ao criar índice único na collection {}: {}", collectionValidos, e.getMessage());
             // Não lançar exceção para não interromper o fluxo principal
         }
     }



     /**
      * Etapa 4: Limpeza e Finalização
      */
     private void executarEtapa4LimpezaFinalizacao(String processoId) {
         ProcessLog log = new ProcessLog(processoId, "Iniciando limpeza e finalização", "LIMPEZA_FINALIZACAO");
         processLogRepository.save(log);
         
         try {
             // Atualizar status do documento na collection loadData para 'processado'
             atualizarStatusLoadData(processoId);
             
             // Deletar coleção temp
             flatMongoTemplate.dropCollection(collectionTemp);
             
             log.setMensagem("Coleção temp removida e status loadData atualizado com sucesso");
             log.finalizarEtapa();
             processLogRepository.save(log);
             
         } catch (Exception e) {
             log.finalizarEtapaComErro(e.getMessage());
             processLogRepository.save(log);
             throw new RuntimeException("Erro na limpeza e finalização", e);
         }
     }
     
     /**
     * Atualiza o status do documento na collection loadData para 'processado'
     * e adiciona o array de procedimentos executados
     */
    private void atualizarStatusLoadData(String processoId) {
        try {
            LocalDate hoje = LocalDate.now();
            String dataAtual = hoje.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            // Buscar todos os logs do processo para criar o array de procedimentos
            List<ProcessLog> logs = processLogRepository.findByProcessoIdOrderByDataInicioAsc(processoId);
            
            // Criar lista de procedimentos executados
            List<Document> procedimentos = new ArrayList<>();
            for (ProcessLog log : logs) {
                if (log.getDataFim() != null && log.getTempoTotalSegundos() != null) {
                    Document procedimento = new Document()
                        .append("nome", log.getEtapa())
                        .append("tempo_execucao_segundos", log.getTempoTotalSegundos())
                        .append("data_execucao", log.getDataInicio());
                    procedimentos.add(procedimento);
                }
            }
            
            Query query = new Query(Criteria.where("data").is(dataAtual).and("status").is("ready"));
            Update update = new Update()
                .set("status", "processado")
                .set("data_processamento", LocalDateTime.now())
                .set("procedimentos_executados", procedimentos);
            
            controlMongoTemplate.updateMulti(query, update, collectionLoadData);
            
            logger.info("Status dos documentos na collection loadData atualizado para 'processado' na data: {} com {} procedimentos", dataAtual, procedimentos.size());
            
        } catch (Exception e) {
            logger.error("Erro ao atualizar status na collection loadData", e);
            throw new RuntimeException("Erro ao atualizar status loadData", e);
        }
    }

     /**
      * Finaliza processo com status específico
      */
     private void finalizarProcessoComStatus(String processoId, ProcessControl.StatusProcesso status) {
         try {
             Optional<ProcessControl> controleOpt = processControlRepository.findByProcessoId(processoId);
             if (controleOpt.isPresent()) {
                 ProcessControl controle = controleOpt.get();
                 controle.setStatus(status);
                 controle.setDataAtualizacao(LocalDateTime.now());
                 processControlRepository.save(controle);
             }
             
             ProcessLog log = new ProcessLog(processoId, "Processo finalizado com status: " + status, "FINALIZACAO");
             log.finalizarEtapa();
             processLogRepository.save(log);
             
         } catch (Exception e) {
             logger.error("Erro ao finalizar processo com status: {}", status, e);
         }
     }

     /**
      * Finaliza processo com erro
      */
     private void finalizarProcessoComErro(String processoId, String erroDetalhes) {
         try {
             Optional<ProcessControl> controleOpt = processControlRepository.findByProcessoId(processoId);
             if (controleOpt.isPresent()) {
                 ProcessControl controle = controleOpt.get();
                 controle.setStatus(ProcessControl.StatusProcesso.ERRO);
                 controle.setErroDetalhes(erroDetalhes);
                 controle.setDataAtualizacao(LocalDateTime.now());
                 processControlRepository.save(controle);
             }
             
             ProcessLog log = new ProcessLog(processoId, "Processo finalizado com erro: " + erroDetalhes, "ERRO", "ERRO");
             log.finalizarEtapa();
             processLogRepository.save(log);
             
         } catch (Exception e) {
             logger.error("Erro ao finalizar processo com erro", e);
         }
     }



    /**
     * Método para execução manual via endpoint
     */
    public ProcessControl executarManualmente() {
        logger.info("Execução manual do fluxo principal solicitada");
        return executarFluxoManual();
    }

    /**
     * Obtém estatísticas do último processamento
     */
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Buscar último processo
            Optional<ProcessControl> ultimoProcessoOpt = processControlRepository.findControlDocument();
            
            if (ultimoProcessoOpt.isPresent()) {
                ProcessControl ultimoProcesso = ultimoProcessoOpt.get();
                stats.put("ultimoProcessoId", ultimoProcesso.getProcessoId());
                stats.put("ultimoStatus", ultimoProcesso.getStatus());
                stats.put("ultimaAtualizacao", ultimoProcesso.getDataAtualizacao());
                stats.put("erroDetalhes", ultimoProcesso.getErroDetalhes());
            }
            
            // Estatísticas de logs
            long totalLogs = processLogRepository.count();
            long logsComErro = processLogRepository.countByStatus("ERRO");
            long logsConcluidos = processLogRepository.countByStatus("CONCLUIDO");
            
            stats.put("totalLogs", totalLogs);
            stats.put("logsComErro", logsComErro);
            stats.put("logsConcluidos", logsConcluidos);
            stats.put("dataConsulta", LocalDateTime.now());
            
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas", e);
            stats.put("erro", e.getMessage());
        }
        
        return stats;
    }

    /**
     * Obtém logs de um processo específico
     */
    public List<ProcessLog> obterLogsProcesso(String processoId) {
        return processLogRepository.findByProcessoIdOrderByDataInicioAsc(processoId);
    }

    /**
     * Obtém status atual do controle de processo
     */
    public Optional<ProcessControl> obterStatusControle() {
        return processControlRepository.findControlDocument();
    }

    /**
     * Limpa logs antigos (mais de 30 dias)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Todo dia às 2h
    public void limparLogsAntigos() {
        try {
            LocalDateTime dataLimite = LocalDateTime.now().minusDays(30);
            processLogRepository.deleteByDataInicioBefore(dataLimite);
            logger.info("Limpeza de logs antigos executada - Data limite: {}", dataLimite);
        } catch (Exception e) {
            logger.error("Erro durante limpeza de logs antigos", e);
        }
    }
}