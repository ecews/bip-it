package com.ecews.bipit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AsyncConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcTaskExecutor()); // Set custom TaskExecutor
        configurer.setDefaultTimeout(30_000); // Optional: set a default timeout (in milliseconds)
    }

    @Bean(name = "mvcTaskExecutor")
    public AsyncTaskExecutor mvcTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Number of core threads
        executor.setMaxPoolSize(50);  // Max number of threads
        executor.setQueueCapacity(100); // Queue size before rejecting new tasks
        executor.setThreadNamePrefix("amos-ecews-"); // Optional: name prefix for threads
        executor.initialize();
        return executor;
    }
}
