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

@RestController
@Slf4j
@RequestMapping("/api/v1/logging")
public class LoggingController {

    private final LoggingService loggingService;

    public LoggingController(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

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