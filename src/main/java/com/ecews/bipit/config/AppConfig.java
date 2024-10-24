package com.ecews.bipit.config;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AppConfig {
   @Bean
   TokenTextSplitter TextSplitter() {
        return new TokenTextSplitter();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Minimum number of threads
        executor.setMaxPoolSize(10); // Maximum number of threads
        executor.setQueueCapacity(25); // Capacity of the queue
        executor.setThreadNamePrefix("ecews_amos_"); // Thread name prefix
        executor.initialize(); // Initialize the executor
        return executor;
    }
}
