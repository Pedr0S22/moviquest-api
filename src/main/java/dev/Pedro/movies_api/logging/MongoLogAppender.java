package dev.Pedro.movies_api.logging;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;
import dev.Pedro.movies_api.configuration.LoggingExecutorProperties;
import dev.Pedro.movies_api.model.LogEvent;
import dev.Pedro.movies_api.service.LogRetryService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MongoLogAppender extends AppenderBase<ILoggingEvent> {
    private MongoLogBuffer buffer;
    private ThreadPoolExecutor executor;
    private LoggingExecutorProperties config;
    private LogRetryService retryService;

    private volatile boolean running = false;
    private volatile Exception lastError = null;
    private final AtomicLong logsProcessedCount = new AtomicLong(0);

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
                            logsProcessedCount.addAndGet(batch.size());
                        }
                        Thread.sleep(config.getRetryDelayMs());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        running = false;
                    } catch (Exception e) {
                        lastError = e;
                        addError("Error saving logs to MongoDB", e);
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

        // Filter out Spring framework logs
        String loggerName = event.getLoggerName();
        if (loggerName.startsWith("org.springframework") ||
                loggerName.startsWith("org.apache") ||
                loggerName.startsWith("ch.qos.logback")) {
            return;
        }

        LogEvent log = toLogEvent(event);
        if (buffer.offer(log)) {
            logsProcessedCount.incrementAndGet();
        } else {
            lastError = new RuntimeException("Buffer full - log dropped");
            addError(lastError.getMessage(), lastError);
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

    public boolean isRunning() {
        return running && !Thread.currentThread().isInterrupted();
    }

    public int getBufferSize() {
        synchronized (this) {
            return buffer != null ? buffer.size() : 0;
        }
    }

    public String getLastError() {
        return lastError != null ? lastError.getClass().getSimpleName() + ": " + lastError.getMessage() : "No errors";
    }

    public long getLogsProcessedCount() {
        return logsProcessedCount.get();
    }
}
