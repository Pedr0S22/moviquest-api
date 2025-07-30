package dev.Pedro.movies_api.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class LogbackAppenderRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    private final MongoLogAppender mongoAppender;

    private static final String[] TARGET_LOG_PACKAGES = {
            "dev.Pedro",
            // "org.springframework.boot.actuate" // -> The root is getting the
            // actuator logs
    };

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Configure the appender
        mongoAppender.setName("MONGO");
        mongoAppender.setContext(context);
        mongoAppender.start();

        // Add to root logger
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(mongoAppender);

        // Add all packages defined earlier
        for (String eachPackage : TARGET_LOG_PACKAGES) {
            Logger packageLogger = context.getLogger(eachPackage);
            packageLogger.addAppender(mongoAppender);
        }

        log.debug("Logback MongoAppenderRegistrar ready");

    }

}