package com.example.atlasdfmongodb.repository;

import com.example.atlasdfmongodb.model.ProcessLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessLogRepository extends MongoRepository<ProcessLog, String> {

    /**
     * Busca logs por processo ID
     */
    List<ProcessLog> findByProcessoIdOrderByDataInicioAsc(String processoId);

    /**
     * Busca logs por etapa
     */
    List<ProcessLog> findByEtapa(String etapa);

    /**
     * Busca logs por status
     */
    List<ProcessLog> findByStatus(String status);

    /**
     * Busca logs em um período específico
     */
    @Query("{'dataInicio': {$gte: ?0, $lte: ?1}}")
    List<ProcessLog> findByDataInicioBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    /**
     * Busca últimos logs por processo
     */
    @Query(value = "{'processoId': ?0}", sort = "{'dataInicio': -1}")
    List<ProcessLog> findLastLogsByProcessoId(String processoId);

    /**
     * Remove logs antigos
     */
    void deleteByDataInicioBefore(LocalDateTime dataLimite);

    /**
     * Conta logs por status
     */
    long countByStatus(String status);
}