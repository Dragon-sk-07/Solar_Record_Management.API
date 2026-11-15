package com.suraj.Customer_Portal_29.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    /** For parallel Cloudinary uploads (used in Part 3). */
    @Bean("cloudinaryPool")
    public Executor cloudinaryPool() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(8);
        ex.setMaxPoolSize(16);
        ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("cloud-");
        ex.initialize();
        return ex;
    }

    /** For parallel PDF generation in All-in-One. */
    @Bean("pdfPool")
    public Executor pdfPool() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(6);
        ex.setMaxPoolSize(12);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("pdf-");
        ex.initialize();
        return ex;
    }
}