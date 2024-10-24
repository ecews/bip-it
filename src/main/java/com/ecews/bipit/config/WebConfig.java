package com.ecews.bipit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Value("${app.env}")
    private String appEnv;  // Inject the environment variable (e.g., "production" or "local")

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                if ("production".equals(appEnv)) {
                    registry.addMapping("/**")
                            .allowedOrigins("http://bip.ecews.org")  // Production URL
                            .allowedMethods("GET", "POST", "PUT", "DELETE")
                            .allowCredentials(true);
                } else {
                    registry.addMapping("/**")
                            .allowedOrigins("http://localhost:3001")  // Local development URL
                            .allowedMethods("GET", "POST", "PUT", "DELETE")
                            .allowCredentials(true);
                }
            }
        };
    }
}
