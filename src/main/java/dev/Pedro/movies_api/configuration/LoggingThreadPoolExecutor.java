package dev.Pedro.movies_api.configuration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.Pedro.movies_api.model.LogEvent;

@Configuration
public class LoggingThreadPoolExecutor {

    @Bean(name = "logExecutor", destroyMethod = "shutdown")
    public ThreadPoolExecutor logExecutor(LoggingExecutorProperties threadProperties) {
        return new ThreadPoolExecutor(
                threadProperties.getCorePoolSize(),
                threadProperties.getMaxPoolSize(),
                threadProperties.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean
    public BlockingQueue<LogEvent> logBuffer(LoggingExecutorProperties bufferProperties) {
        return new LinkedBlockingQueue<>(bufferProperties.getBufferCapacity());
    }
}
