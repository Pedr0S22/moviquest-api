package dev.Pedro.movies_api.service;

import dev.Pedro.movies_api.logging.MongoLogBuffer;
import dev.Pedro.movies_api.model.LogEvent;
import dev.Pedro.movies_api.repository.LogEventRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service class responsible for saving log events into MongoDB.
 * This class uses Resilience4j annotations to apply fault tolerance mechanisms:
 * <ul>
 * <li>{@code Retry:} retries saving if an exception occurs.</li>
 * <li>{@code CircuitBreaker:} stops requests temporarily if failures exceed a
 * threshold.</li>
 * <li>{@code TimeLimiter: enforces a maximum timeout for save operations.}</li>
 * </ul>
 */
@Service
@Slf4j
public class LoggingService {

    private final LogEventRepository logEventRepository;
    private final MongoLogBuffer buffer;

    /**
     * Constructor injection of dependencies.
     *
     * @param logEventRepository the repository for storing log events.
     * @param buffer             the buffer used for requeuing failed logs.
     */
    public LoggingService(LogEventRepository logEventRepository, MongoLogBuffer buffer) {
        this.logEventRepository = logEventRepository;
        this.buffer = buffer;
    }

    /**
     * Asynchronously saves logs to MongoDB with retry, circuit breaker, and time
     * limiter protection.
     *
     * @param logs list of log events to save.
     * @return a CompletableFuture representing the async operation.
     */
    @Retry(name = "mongoRetry", fallbackMethod = "fallback")
    @CircuitBreaker(name = "mongoCB", fallbackMethod = "fallback")
    @TimeLimiter(name = "timelimiter", fallbackMethod = "fallback")
    public CompletableFuture<Void> saveLogs(List<LogEvent> logs) {
        return CompletableFuture.runAsync(() -> {
            logEventRepository.saveAll(logs);
        });
    }

    /**
     * Fallback method used when saveLogs fails due to exceptions or timeouts.
     *
     * @param logs the logs that failed to save.
     * @param t    the throwable that caused the fallback.
     * @return a completed CompletableFuture.
     */
    public CompletableFuture<Void> fallback(List<LogEvent> logs, Throwable t) {
        log.warn("[LoggingService] Fallback triggered, requeuing logs {} due to: "
                + t.getClass().getSimpleName() + " - " + t.getMessage(), logs.size());
        buffer.requeue(logs);
        return CompletableFuture.completedFuture(null);
    }
}
