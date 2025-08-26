package dev.Pedro.movies_api.service;

import dev.Pedro.movies_api.dto.request.SearchLogRequest;
import dev.Pedro.movies_api.logging.MongoLogBuffer;
import dev.Pedro.movies_api.model.LogEvent;
import dev.Pedro.movies_api.repository.LogEventRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for saving and querying log events in MongoDB.
 * <p>
 * Provides:
 * <ul>
 * <li>Asynchronous log persistence with Resilience4j fault tolerance mechanisms
 * (Retry, CircuitBreaker, TimeLimiter).</li>
 * <li>Search functionality using flexible filters including log level,
 * timestamp, thread, logger, MDC, and keywords.</li>
 * <li>Fallback handling that requeues logs in a buffer when persistence
 * fails.</li>
 * </ul>
 */
@Service
@Slf4j
public class LoggingService {

    private final LogEventRepository logEventRepository;
    private final MongoLogBuffer buffer;

    private final MongoTemplate mongoTemplate;

    /**
     * Constructor injection of dependencies.
     *
     * @param logEventRepository the repository for storing log events.
     * @param buffer             the buffer used for requeuing failed logs.
     */
    public LoggingService(LogEventRepository logEventRepository, MongoLogBuffer buffer, MongoTemplate mongoTemplate) {
        this.logEventRepository = logEventRepository;
        this.buffer = buffer;
        this.mongoTemplate = mongoTemplate;
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

    /**
     * Searches MongoDB for logs matching filters specified in
     * {@link SearchLogRequest}.
     * <p>
     * Supports filtering by:
     * <ul>
     * <li>Log level</li>
     * <li>Timestamp range</li>
     * <li>Thread</li>
     * <li>Logger</li>
     * <li>MDC (Mapped Diagnostic Context)</li>
     * <li>Message keywords (case-insensitive)</li>
     * </ul>
     * Can optionally sort results by timestamp.
     *
     * @param logRequest the search criteria
     * @return a list of matching {@link Document} objects from MongoDB
     */
    public List<Document> searchLogs(SearchLogRequest logRequest) {

        List<Criteria> criteriaList = new ArrayList<>();

        // Match log level
        if (logRequest.getLevel() != null && !logRequest.getLevel().isBlank()) {
            criteriaList.add(Criteria.where("level").is(logRequest.getLevel().toUpperCase()));
        }

        // Timestamp filtering logic
        LocalDateTime after = logRequest.getTimestampAfter();
        LocalDateTime before = logRequest.getTimestampBefore();

        if (after != null && before != null) {
            criteriaList.add(
                    Criteria.where("localDateTime")
                            .gte(toDateUTC(after))
                            .lte(toDateUTC(before)));
        } else if (after != null) {
            criteriaList.add(
                    Criteria.where("localDateTime").gte(toDateUTC(after)));
        } else if (before != null) {
            criteriaList.add(
                    Criteria.where("localDateTime").lte(toDateUTC(before)));
        }
        // Search thread
        if (logRequest.getThread() != null && !logRequest.getThread().isBlank()) {
            criteriaList.add(Criteria.where("thread").regex(logRequest.getThread(), "i"));
        }

        // Search logger
        if (logRequest.getLogger() != null && !logRequest.getLogger().isBlank()) {
            criteriaList.add(Criteria.where("logger").regex(logRequest.getLogger(), "i"));
        }

        // Match MDC object (exact match)
        if (logRequest.getMdc() != null) {
            criteriaList.add(Criteria.where("mdc").is(logRequest.getMdc()));
        }

        // Search keywords
        if (logRequest.getMessageKeywords() != null && !logRequest.getMessageKeywords().isBlank()) {
            criteriaList.add(Criteria.where("message").regex(logRequest.getMessageKeywords(), "i"));
        }

        // Build the query
        Criteria finalCriteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            finalCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(finalCriteria);

        // Sort conditionally
        if (logRequest.isSortByTimestamp()) {
            query.with(Sort.by(Sort.Direction.ASC, "localDateTime"));
        }

        return mongoTemplate.find(query, Document.class, "logEvents");
    }

    /**
     * Converts a {@link LocalDateTime} to a UTC {@link Date}.
     *
     * @param ldt the LocalDateTime to convert
     * @return a Date object representing the same instant in UTC
     */
    private Date toDateUTC(LocalDateTime ldt) {
        return Date.from(ldt.toInstant(ZoneOffset.UTC));
    }

}
