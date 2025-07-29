package dev.Pedro.movies_api.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.Pedro.movies_api.logging.MongoLogAppender;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/actuator/logging")
@RequiredArgsConstructor
public class LoggingController {
    private final MongoLogAppender mongoAppender;

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return Map.of(
                "running", mongoAppender.isRunning(),
                "bufferSize", mongoAppender.getBufferSize(),
                "lastError", mongoAppender.getLastError(),
                "logsProcessed", mongoAppender.getLogsProcessedCount());
    }
}