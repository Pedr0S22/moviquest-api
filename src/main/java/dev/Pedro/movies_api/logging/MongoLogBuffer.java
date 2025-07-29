package dev.Pedro.movies_api.logging;

import dev.Pedro.movies_api.model.LogEvent;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MongoLogBuffer {
    private final BlockingQueue<LogEvent> buffer;
    private final AtomicInteger currentSize = new AtomicInteger(0);

    public MongoLogBuffer(@Qualifier("logBuffer") BlockingQueue<LogEvent> buffer) {
        this.buffer = buffer;
    }

    public List<LogEvent> drainBatch(int size) {
        List<LogEvent> batch = new ArrayList<>(size);
        try {
            LogEvent first = buffer.poll(100, TimeUnit.MILLISECONDS);
            if (first != null) {
                batch.add(first);
                buffer.drainTo(batch, size - 1);
                currentSize.addAndGet(-batch.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return batch;
    }

    public boolean offer(LogEvent log) {
        boolean offered = buffer.offer(log);
        if (offered) {
            currentSize.incrementAndGet();
        }
        return offered;
    }

    public int size() {
        return currentSize.get();
    }

    public void requeue(List<LogEvent> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }

        int requeued = 0;
        for (LogEvent log : logs) {
            if (log != null && buffer.offer(log)) {
                requeued++;
            }
        }
        currentSize.addAndGet(requeued);

        if (requeued < logs.size()) {
            System.err.println("[MongoLogBuffer] Failed to requeue " +
                    (logs.size() - requeued) + " logs (buffer full)");
        }
    }

}