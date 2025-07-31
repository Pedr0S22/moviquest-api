package dev.Pedro.movies_api.service;

import dev.Pedro.movies_api.logging.MongoLogBuffer;
import dev.Pedro.movies_api.model.LogEvent;
import dev.Pedro.movies_api.repository.LogEventRepository;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        System.err.println("[LogRetryService] Fallback triggered, requeuing logs due to: "
                + t.getClass().getSimpleName() + " - " + t.getMessage());
        buffer.requeue(logs);
    }
}
