package dev.Pedro.movies_api.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a log event stored in the MongoDB collection "logEvents".
 * <p>
 * This class captures logging metadata including log level, logger name,
 * message, thread, MDC and timestamp.
 * </p>
 */
@Document(collection = "logEvents")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogEvent {

    @Id
    private ObjectId logId;

    private LocalDateTime localDateTime;
    private String level;
    private String logger;
    private String thread;
    private String message;
    private Map<String, String> mdc;

    /**
     * Constructs a LogEvent with basic information. MDC is not included.
     * The timestamp is set to the current time.
     *
     * @param logId   Unique ID for the log.
     * @param level   Log level.
     * @param logger  Logger name.
     * @param thread  Thread name.
     * @param message Log message.
     */
    public LogEvent(ObjectId logId, String level, String logger, String thread, String message) {
        this.logId = logId;
        this.localDateTime = LocalDateTime.now();
        this.level = level;
        this.logger = logger;
        this.thread = thread;
        this.message = message;
    }

    /**
     * Constructs a LogEvent with MDC context.
     * The timestamp is set to the current time.
     *
     * @param logId   Unique ID for the log.
     * @param level   Log level.
     * @param logger  Logger name.
     * @param thread  Thread name.
     * @param message Log message.
     * @param mdc     Mapped Diagnostic Context metadata.
     */
    public LogEvent(ObjectId logId, String level, String logger, String thread, String message,
            Map<String, String> mdc) {

        this.logId = logId;
        this.localDateTime = LocalDateTime.now();
        this.level = level;
        this.logger = logger;
        this.thread = thread;
        this.message = message;
        this.mdc = mdc;
    }
}
