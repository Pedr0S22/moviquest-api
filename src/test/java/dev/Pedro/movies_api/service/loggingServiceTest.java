package dev.Pedro.movies_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import dev.Pedro.movies_api.dto.request.SearchLogRequest;
import dev.Pedro.movies_api.logging.MongoLogBuffer;
import dev.Pedro.movies_api.model.LogEvent;
import dev.Pedro.movies_api.repository.LogEventRepository;

@ExtendWith(MockitoExtension.class)
public class loggingServiceTest {
    @Mock
    private LogEventRepository logEventRepository;

    @Mock
    private MongoLogBuffer buffer;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private LoggingService loggingService;

    private LogEvent sampleLog;
    private SearchLogRequest request;

    @BeforeEach
    void setup() {
        sampleLog = new LogEvent();
        request = new SearchLogRequest();
    }

    @Test
    void testSaveLogsSuccess() throws Exception {
        List<LogEvent> logs = List.of(sampleLog);

        when(logEventRepository.saveAll(logs)).thenReturn(logs);

        // Wait for async execution
        CompletableFuture<Void> result = loggingService.saveLogs(logs);
        result.join();

        verify(logEventRepository, times(1)).saveAll(logs);
    }

    @Test
    void testFallbackRequeuesLogs() {
        List<LogEvent> logs = List.of(sampleLog);
        Throwable cause = new RuntimeException("DB down");

        loggingService.fallback(logs, cause);

        verify(buffer, times(1)).requeue(logs);
    }

    @Test
    void testSearchLogs_ByLevelAndThread() {

        request.setLevel("info");
        request.setThread("main");
        request.setSortByTimestamp(true);

        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("logEvents")))
                .thenReturn(List.of(new Document("message", "Hello")));

        List<Document> results = loggingService.searchLogs(request);

        assertEquals(1, results.size());
        assertEquals("Hello", results.get(0).get("message"));

        verify(mongoTemplate, times(1))
                .find(any(Query.class), eq(Document.class), eq("logEvents"));
    }

    @Test
    void testSearchLogs_ByTimestampRange() {

        request.setTimestampAfter(LocalDateTime.now().minusDays(1));
        request.setTimestampBefore(LocalDateTime.now());

        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("logEvents")))
                .thenReturn(List.of());

        List<Document> results = loggingService.searchLogs(request);

        assertNotNull(results);
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(Document.class), eq("logEvents"));
    }

    @Test
    void testSearchLogs_withOnlyAfter() {

        LocalDateTime after = LocalDateTime.now().minusDays(1);
        request.setTimestampAfter(after);

        Document fakeDoc = new Document("message", "after only");
        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("logEvents")))
                .thenReturn(List.of(fakeDoc));

        List<Document> results = loggingService.searchLogs(request);

        assertEquals(1, results.size());
        assertEquals("after only", results.get(0).getString("message"));
        verify(mongoTemplate, times(1))
                .find(any(Query.class), eq(Document.class), eq("logEvents"));
    }

    @Test
    void testSearchLogs_withOnlyBefore() {

        LocalDateTime before = LocalDateTime.now();
        request.setTimestampBefore(before);

        Document fakeDoc = new Document("message", "before only");
        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("logEvents")))
                .thenReturn(List.of(fakeDoc));

        List<Document> results = loggingService.searchLogs(request);

        assertEquals(1, results.size());
        assertEquals("before only", results.get(0).getString("message"));
        verify(mongoTemplate, times(1))
                .find(any(Query.class), eq(Document.class), eq("logEvents"));
    }
}
