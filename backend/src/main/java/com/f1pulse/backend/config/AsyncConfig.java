package com.deltabox.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async task execution
 * Enables @Async annotation support and configures thread pool
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configure thread pool for async tasks
     */
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-ingestion-");
        executor.initialize();
        return executor;
    }
}
