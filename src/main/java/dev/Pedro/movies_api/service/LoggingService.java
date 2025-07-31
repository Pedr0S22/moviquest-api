package dev.Pedro.movies_api.service;

import dev.Pedro.movies_api.logging.MongoLogBuffer;
import dev.Pedro.movies_api.model.LogEvent;
import dev.Pedro.movies_api.repository.LogEventRepository;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LoggingService {

    private final LogEventRepository repository;
    private final MongoLogBuffer buffer;

    public LoggingService(LogEventRepository repository, MongoLogBuffer buffer) {
        this.repository = repository;
        this.buffer = buffer;
    }

    @Retry(name = "mongoRetry", fallbackMethod = "fallback")
    public void saveLogs(List<LogEvent> logs) {
        repository.saveAll(logs);
    }

    public void fallback(List<LogEvent> logs, Throwable t) {
        log.warn("[LoggingService] Fallback triggered, requeuing logs {} due to: "
                + t.getClass().getSimpleName() + " - " + t.getMessage(), logs.size());
        buffer.requeue(logs);
    }
}
