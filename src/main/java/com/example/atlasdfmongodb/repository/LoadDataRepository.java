package com.example.atlasdfmongodb.repository;

import com.example.atlasdfmongodb.model.LoadData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoadDataRepository extends MongoRepository<LoadData, String> {

    /**
     * Busca documento por data e status
     */
    Optional<LoadData> findByDataAndStatus(String data, String status);

    /**
     * Busca todos os documentos por data
     */
    List<LoadData> findByData(String data);

    /**
     * Busca todos os documentos por status
     */
    List<LoadData> findByStatus(String status);

    /**
     * Busca documentos prontos para uma data específica
     */
    @Query("{'data': ?0, 'status': 'pronto'}")
    List<LoadData> findProntosByData(String data);

    /**
     * Busca documentos processados
     */
    @Query("{'status': 'processado'}")
    List<LoadData> findProcessados();

    /**
     * Conta documentos por status
     */
    long countByStatus(String status);

    /**
     * Verifica se existe documento pronto para uma data
     */
    boolean existsByDataAndStatus(String data, String status);
    
    /**
     * Verifica se existe documento pronto para uma data específica
     */
    default boolean existsProntoByData(String data) {
        return existsByDataAndStatus(data, "pronto");
    }
}