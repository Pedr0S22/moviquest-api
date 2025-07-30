package dev.Pedro.movies_api.configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.Pedro.movies_api.model.LogEvent;

/**
 * <p>
 * Configuration class that defines Spring beans for managing asynchronous
 * logging to Mongo Database.
 * </p>
 *
 * <p>
 * Provides a customized {@link ThreadPoolExecutor} bean configured with
 * parameters that handle the execution of logging tasks into the database.
 * </p>
 *
 * <p>
 * Also defines a bounded {@link BlockingQueue} ({@link LinkedBlockingQueue}) to
 * buffer {@link LogEvent} objects before they are processed, sized and batched
 * according to the configured buffer capacity and batching size.
 * </p>
 */
@Configuration
public class LoggingThreadPoolExecutor {

    /**
     * Creates a {@link ThreadPoolExecutor} bean named "logExecutor" for processing
     * logging tasks.
     * The thread pool parameters are injected from
     * {@link LoggingExecutorProperties}. Also, when the {@code shutdown} method is
     * launched,
     * this bean will be destroyed.
     *
     * @param threadProperties configuration properties for thread pool behavior.
     * @return a configured ThreadPoolExecutor instance.
     */
    @Bean(name = "logExecutor", destroyMethod = "shutdown")
    public ThreadPoolExecutor logExecutor(LoggingExecutorProperties threadProperties) {
        return new ThreadPoolExecutor(
                threadProperties.getCorePoolSize(),
                threadProperties.getMaxPoolSize(),
                threadProperties.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Creates a bounded {@link BlockingQueue} ({@link LinkedBlockingQueue}) bean
     * named "logBuffer" to hold {@link LogEvent} instances.
     * This queue serves as a buffer for asynchronous logging, temporarily storing
     * log events before they are batched and saved to the database. The buffer
     * capacity is set based on configuration properties.
     *
     * @param bufferProperties configuration properties providing the buffer
     *                         capacity
     * @return a LinkedBlockingQueue with the configured capacity
     */
    @Bean
    public BlockingQueue<LogEvent> logBuffer(LoggingExecutorProperties bufferProperties) {
        return new LinkedBlockingQueue<>(bufferProperties.getBufferCapacity());
    }
}
