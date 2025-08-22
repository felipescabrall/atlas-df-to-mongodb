package com.example.atlasdfmongodb.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfig {

    // Conexão Principal - Banco Bradesco
    @Value("${spring.data.mongodb.uri}")
    private String primaryMongoUri;

    @Value("${spring.data.mongodb.database}")
    private String primaryDatabase;

    // Conexão Flat - Banco Bradesco Flat
    @Value("${mongodb.flat.uri}")
    private String flatMongoUri;

    @Value("${mongodb.flat.database}")
    private String flatDatabase;

    // Conexão Controle - Banco de Controle e Logs
    @Value("${mongodb.control.uri}")
    private String controlMongoUri;

    @Value("${mongodb.control.database}")
    private String controlDatabase;

    @Bean
    @Primary
    public MongoClient primaryMongoClient() {
        return MongoClients.create(primaryMongoUri);
    }

    @Bean
    public MongoClient flatMongoClient() {
        return MongoClients.create(flatMongoUri);
    }

    @Bean
    public MongoClient controlMongoClient() {
        return MongoClients.create(controlMongoUri);
    }

    @Bean(name = {"mongoTemplate", "primaryMongoTemplate"})
    @Primary
    public MongoTemplate primaryMongoTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(primaryMongoClient(), primaryDatabase));
    }

    @Bean(name = "flatMongoTemplate")
    public MongoTemplate flatMongoTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(flatMongoClient(), flatDatabase));
    }

    @Bean(name = "controlMongoTemplate")
    public MongoTemplate controlMongoTemplate() {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(controlMongoClient(), controlDatabase));
    }
}