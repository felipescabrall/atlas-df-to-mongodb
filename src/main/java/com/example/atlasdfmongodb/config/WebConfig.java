package com.example.atlasdfmongodb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redireciona a raiz (/) para /api
        registry.addRedirectViewController("/", "/api");
        // Também redireciona /index para /api (caso alguém acesse)
        registry.addRedirectViewController("/index", "/api");
    }
}