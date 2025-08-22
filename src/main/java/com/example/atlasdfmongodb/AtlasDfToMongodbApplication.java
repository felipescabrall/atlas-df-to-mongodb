package com.example.atlasdfmongodb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableMongoRepositories(
    basePackages = "com.example.atlasdfmongodb.repository",
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.REGEX,
        pattern = ".*ProcessControlRepository|.*ProcessLogRepository"
    )
)
public class AtlasDfToMongodbApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasDfToMongodbApplication.class, args);
    }

}