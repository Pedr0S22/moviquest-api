package dev.Pedro.movies_api.logging.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * <p>
 * Configuration class that fetches all configurations settings related to
 * Thread Pool and Log Batching from {@code application.properties} using
 * {@link ConfigurationProperties}.
 * </p>
 *
 * *
 * <p>
 * Properties are bound from those prefixed with <strong>logging.</strong>:
 * </p>
 *
 * <ul>
 * <li><strong>Thread Pool:</strong>
 * <ul>
 * <li>{@code logging.core-pool-size}</li>
 * <li>{@code logging.max-pool-size}</li>
 * <li>{@code logging.keep-alive-seconds}</li>
 * </ul>
 * </li>
 * <li><strong>Log Batching:</strong>
 * <ul>
 * <li>{@code logging.batch-size}</li>
 * <li>{@code logging.max-retries}</li>
 * <li>{@code logging.retry-delay-ms}</li>
 * <li>{@code logging.buffer-capacity}</li>
 * </ul>
 * </li>
 * </ul>
 *
 * <p>
 * This class is automatically registered as a Spring bean and used to inject
 * log-related configuration wherever needed.
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "logging")
@Data
public class LoggingVariables {

    // Thread Pool
    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveSeconds;

    // Log batching
    private int batchSize;
    private int retryDelayMs;
    private int bufferCapacity;
}