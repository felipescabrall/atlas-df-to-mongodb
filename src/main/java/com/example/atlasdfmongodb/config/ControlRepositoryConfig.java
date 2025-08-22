package com.example.atlasdfmongodb.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
    basePackages = "com.example.atlasdfmongodb.repository",
    mongoTemplateRef = "controlMongoTemplate",
    includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.REGEX,
        pattern = ".*ProcessControlRepository|.*ProcessLogRepository"
    )
)
public class ControlRepositoryConfig {
    // Esta configuração garante que os repositórios ProcessControlRepository e ProcessLogRepository
    // usem o controlMongoTemplate, direcionando-os para o banco de dados control_db
}