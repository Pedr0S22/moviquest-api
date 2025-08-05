package dev.Pedro.movies_api.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import dev.Pedro.movies_api.logging.configuration.LoggingVariables;
import dev.Pedro.movies_api.model.LogEvent;
import dev.Pedro.movies_api.service.LoggingService;
import lombok.extern.slf4j.Slf4j;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A custom Logback appender that batches log events and writes them to MongoDB
 * asynchronously using a background thread pool.
 */
@Component
@Slf4j
public class MongoLogAppender extends AppenderBase<ILoggingEvent> {

    private MongoLogBuffer buffer;
    private ThreadPoolExecutor executor;
    private LoggingVariables config;
    private LoggingService loggingService;

    /**
     * Flag to control the background logging loop.
     */
    private volatile boolean running = false;

    /**
     * Default no-args constructor required by Logback for instantiating appenders.
     */
    public MongoLogAppender() {
    }

    /**
     * Injects required dependencies via setter. Used instead of constructor
     * injection because Logback requires a no-args constructor.
     *
     * @param buffer         the buffer for holding log events before persistence
     * @param executor       the thread pool to run background log-saving task
     * @param config         configuration values such as batch size and retry delay
     * @param loggingService service responsible for persisting logs to MongoDB
     */
    @Autowired
    public void setDependencies(
            MongoLogBuffer buffer,
            @Qualifier("logExecutor") ThreadPoolExecutor executor,
            LoggingVariables config,
            LoggingService loggingService) {
        this.buffer = buffer;
        this.executor = executor;
        this.config = config;
        this.loggingService = loggingService;
    }

    /**
     * Starts the custom appender. Submits a background task to drain
     * the buffer and persist logs.
     */
    @Override
    public void start() {
        if (!isStarted()) {
            super.start();
            running = true;

            executor.submit(() -> {
                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        List<LogEvent> batch = buffer.drainBatch(config.getBatchSize());
                        if (!batch.isEmpty()) {
                            loggingService.saveLogs(batch);
                        }
                        Thread.sleep(config.getRetryDelayMs());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        running = false;
                    } catch (Exception e) {
                        String message = "Error saving logs to MongoDB";
                        log.error(message, e);
                        addError(message, e);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            break;
                        }
                    }
                }
            });
        }
    }

    /**
     * Appends a log event to the internal buffer.
     *
     * @param event the log event to append
     */
    @Override
    protected void append(ILoggingEvent event) {
        // Skip if not started or buffer not ready
        if (!isStarted() || buffer == null)
            return;

        LogEvent logEvent = toLogEvent(event);
        if (!buffer.offerAndVerify(logEvent)) {
            String message = "Buffer full - log dropped";
            log.error(message);
            addError(message);
        }
    }

    /**
     * Stops the background logging thread and the appender itself.
     */
    @Override
    public void stop() {
        running = false;
        super.stop();
    }

    /**
     * Converts a Logback {@link ILoggingEvent] into a custom {@link LogEvent}
     * object suitable for MongoDB persistence.
     *
     * @param event the logging event
     * @return a new `LogEvent` with extracted information
     */
    private LogEvent toLogEvent(ILoggingEvent event) {
        return new LogEvent(
                ObjectId.get(),
                event.getLevel().toString(),
                event.getLoggerName(),
                event.getThreadName(),
                event.getFormattedMessage(),
                event.getMDCPropertyMap());
    }
}
