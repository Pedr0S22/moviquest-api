package dev.Pedro.movies_api.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import dev.Pedro.movies_api.configuration.LoggingExecutorProperties;
import dev.Pedro.movies_api.model.LogEvent;
import dev.Pedro.movies_api.service.LogRetryService;
import lombok.extern.slf4j.Slf4j;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class MongoLogAppender extends AppenderBase<ILoggingEvent> {

    private MongoLogBuffer buffer;
    private ThreadPoolExecutor executor;
    private LoggingExecutorProperties config;
    private LogRetryService retryService;

    private volatile boolean running = false;

    // Required for Logback initialization
    public MongoLogAppender() {
    }

    @Autowired
    public void setDependencies(
            MongoLogBuffer buffer,
            @Qualifier("logExecutor") ThreadPoolExecutor executor,
            LoggingExecutorProperties config,
            LogRetryService retryService) {
        this.buffer = buffer;
        this.executor = executor;
        this.config = config;
        this.retryService = retryService;
    }

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
                            retryService.saveLogs(batch);
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

    @Override
    public void stop() {
        running = false;
        super.stop();
    }

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
