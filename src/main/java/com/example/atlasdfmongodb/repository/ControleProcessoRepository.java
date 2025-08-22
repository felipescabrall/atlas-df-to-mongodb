package com.example.atlasdfmongodb.repository;

import com.example.atlasdfmongodb.model.ControleProcesso;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ControleProcessoRepository extends MongoRepository<ControleProcesso, String> {

    /**
     * Busca processo por ID do processo
     */
    Optional<ControleProcesso> findByProcessoId(String processoId);

    /**
     * Busca processos por status
     */
    List<ControleProcesso> findByStatus(ControleProcesso.StatusProcesso status);

    /**
     * Busca processos por nome do processo
     */
    List<ControleProcesso> findByNomeProcesso(String nomeProcesso);

    /**
     * Busca processos em um período específico
     */
    @Query("{'dataInicio': {$gte: ?0, $lte: ?1}}")
    List<ControleProcesso> findByDataInicioBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    /**
     * Busca processos ativos (não finalizados)
     */
    @Query("{'status': {$in: ['INICIADO', 'EM_ANDAMENTO']}}")
    List<ControleProcesso> findProcessosAtivos();

    /**
     * Busca processos com erro
     */
    List<ControleProcesso> findByStatusAndRegistrosErroGreaterThan(ControleProcesso.StatusProcesso status, Long registrosErro);

    /**
     * Busca último processo por nome
     */
    @Query(value = "{'nomeProcesso': ?0}", sort = "{'dataCriacao': -1}")
    Optional<ControleProcesso> findLastByNomeProcesso(String nomeProcesso);

    /**
     * Conta processos por status
     */
    long countByStatus(ControleProcesso.StatusProcesso status);

    /**
     * Remove processos antigos
     */
    void deleteByDataCriacaoBefore(LocalDateTime dataLimite);

    /**
     * Busca processos ordenados por data de criação (mais recentes primeiro)
     */
    List<ControleProcesso> findAllByOrderByDataCriacaoDesc();

    /**
     * Busca processos com paginação por status
     */
    @Query(value = "{'status': ?0}", sort = "{'dataCriacao': -1}")
    List<ControleProcesso> findByStatusOrderByDataCriacaoDesc(ControleProcesso.StatusProcesso status);
}