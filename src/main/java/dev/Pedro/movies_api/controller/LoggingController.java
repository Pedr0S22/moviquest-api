package dev.Pedro.movies_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.Pedro.movies_api.dto.request.SearchLogRequest;
import dev.Pedro.movies_api.service.LoggingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller that provides access to application logs.
 * <p>
 * This controller is mainly intended for administrative use and allows
 * filtered log searching through the {@link LoggingService}.
 * </p>
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/logging")
public class LoggingController {

    private final LoggingService loggingService;

    /**
     * Creates a new {@code LoggingController} with the required logging service.
     *
     * @param loggingService the service responsible for executing log queries
     */
    public LoggingController(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Searches application logs using the given filter criteria.
     * <p>
     * Only accessible to users with the {@code ADMIN} role.
     * The method logs the request parameters for traceability,
     * delegates the search to {@link LoggingService},
     * and returns the results in a {@link ResponseEntity}.
     * </p>
     *
     * @param logRequest the search filter request, including level, timestamp
     *                   range,
     *                   thread, logger, MDC values, keywords, and sorting options
     * @return a {@code ResponseEntity} containing a list of log entries
     *         as {@link Document} objects that match the given criteria
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Document>> searchLogsFilter(@Valid @RequestBody SearchLogRequest logRequest) {

        long start = System.currentTimeMillis();

        log.info("Request received to access Logs with filters: {}, {}, {}, {}, {}, {}, {}, {}", logRequest.getLevel(),
                logRequest.getTimestampAfter(),
                logRequest.getTimestampBefore(),
                logRequest.getThread(),
                logRequest.getLogger(),
                logRequest.getMdc() != null ? logRequest.getMdc().toString() : null,
                logRequest.getMessageKeywords(),
                logRequest.isSortByTimestamp());

        List<Document> logs = loggingService.searchLogs(logRequest);

        long duration = System.currentTimeMillis() - start;
        log.info("Search Logs completed. Returning {} logs. Search duration: {}ms", logs.size(), duration);

        return ResponseEntity.ok(logs);
    }

}