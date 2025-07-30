package dev.Pedro.movies_api.logging;

import dev.Pedro.movies_api.model.LogEvent;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Component that manages a buffer for {@link LogEvent} objects using a
 * {@link BlockingQueue}. Provides batch retrieval, insertion, and requeueing
 * of log events to support asynchronous logging to MongoDB.
 * <p>
 * The buffer operates as an intermediary holding area for log events before
 * they are processed and persisted. This class wraps the injected
 * {@code BlockingQueue<LogEvent>} bean named {@code "logBuffer"} and adds
 * convenience methods to batch drain events and safely requeue failed logs.
 * </p>
 */
@Component
@Slf4j
public class MongoLogBuffer {

    /**
     * The thread-safe buffer that holds log events.
     */
    private final BlockingQueue<LogEvent> buffer;

    /**
     * Constructs a new {@code MongoLogBuffer} using the specified buffer bean.
     *
     * @param buffer the {@code BlockingQueue<LogEvent>} bean named "logBuffer"
     */
    public MongoLogBuffer(@Qualifier("logBuffer") BlockingQueue<LogEvent> buffer) {
        this.buffer = buffer;
    }

    /**
     * Retrieves up to {@code size} log events from the buffer as a batch.
     * <p>
     * This method attempts to retrieve the first event with a timeout of 100
     * milliseconds,
     * then drains up to {@code size - 1} additional events to the batch without
     * blocking.
     * This helps efficiently collect log events in batches for processing.
     * </p>
     *
     * @param size the maximum number of log events to retrieve in the batch
     * @return a list of log events up to the requested batch size; may be empty if
     *         no logs available
     */
    public List<LogEvent> drainBatch(int size) {
        List<LogEvent> batch = new ArrayList<>(size);
        try {
            LogEvent first = buffer.poll(100, TimeUnit.MILLISECONDS);
            if (first != null) {
                batch.add(first);
                buffer.drainTo(batch, size - 1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[MongoLogBuffer] Something went wrong with drainBatch {} logs.", size, e);
        }
        return batch;
    }

    /**
     * Attempts to add a new log event to the buffer without blocking and retrieve
     * a boolean whether the log was offered or not.
     *
     * @param log the log event to add.
     * @return {@code true} if the event was successfully added; {@code false} if
     *         the buffer is full.
     */
    public boolean offerAndVerify(LogEvent log) {
        boolean offered = buffer.offer(log);
        return offered;
    }

    /**
     * Attempts to add a new log event to the buffer without blocking.
     *
     * @param log the log event to add.
     * @return void.
     */
    public void offer(LogEvent log) {
        buffer.offer(log);
        return;
    }

    /**
     * Attempts to requeue a list of log events back into the buffer, typically used
     * when processing previously drained events failed and they need to be retried.
     * <p>
     * Logs the number of successfully requeued logs and any failures due to a full
     * buffer.
     * </p>
     *
     * @param logs the list of log events to requeue; if {@code null} or empty, this
     *             method returns immediately.
     */
    public void requeue(List<LogEvent> logs) {

        int logsSize = logs.size();

        if (logs == null || logs.isEmpty())
            return;

        log.info("[MongoLogBuffer] Trying to requeue {} logs into buffer", logsSize);

        int requeued = 0;
        for (LogEvent log : logs) {
            if (log != null && buffer.offer(log))
                requeued++;
        }

        if (requeued < logsSize) {
            log.error("[MongoLogBuffer] Failed to requeue {} logs (buffer full)", (logsSize - requeued));
            return;
        }
        log.info("[MongoLogBuffer] Requeued {} logs successfully", requeued);
    }
}