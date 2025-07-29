package dev.Pedro.movies_api.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "logging")
@Data
public class LoggingExecutorProperties {

    // Thread Pool
    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveSeconds;

    // Log batching
    private int batchSize;
    private int maxRetries;
    private int retryDelayMs;
    private int bufferCapacity;
}