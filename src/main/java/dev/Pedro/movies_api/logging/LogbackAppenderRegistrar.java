package dev.Pedro.movies_api.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
@RequiredArgsConstructor
public class LogbackAppenderRegistrar implements ApplicationListener<ApplicationReadyEvent> {
    private final MongoLogAppender mongoAppender;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Remove any existing appender with this name
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAppender("MONGO");

        // Configure the appender
        mongoAppender.setName("MONGO");
        mongoAppender.setContext(context);
        mongoAppender.start();

        // Add to root logger
        rootLogger.addAppender(mongoAppender);

        // Add to your package-specific logger too
        Logger packageLogger = context.getLogger("dev.Pedro");
        packageLogger.addAppender(mongoAppender);
    }
}