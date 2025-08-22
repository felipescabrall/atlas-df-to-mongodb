package com.example.atlasdfmongodb.repository;

import com.example.atlasdfmongodb.model.ProcessControl;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessControlRepository extends MongoRepository<ProcessControl, String> {

    /**
     * Busca o documento de controle único do sistema
     * Como deve haver apenas um documento de controle, busca o primeiro
     */
    @Query("{}")
    Optional<ProcessControl> findControlDocument();

    /**
     * Busca por processo ID específico
     */
    Optional<ProcessControl> findByProcessoId(String processoId);

    /**
     * Busca por status específico
     */
    Optional<ProcessControl> findByStatus(ProcessControl.StatusProcesso status);
    
    /**
     * Busca todos os processos com um determinado status
     */
    List<ProcessControl> findAllByStatus(ProcessControl.StatusProcesso status);

    /**
     * Conta documentos por status
     */
    long countByStatus(ProcessControl.StatusProcesso status);
}