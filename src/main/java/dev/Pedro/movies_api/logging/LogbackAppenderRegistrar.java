package dev.Pedro.movies_api.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.event.ApplicationReadyEvent;

/**
 * Spring component that registers the custom {@link MongoLogAppender} with
 * Logback loggers once the application is ready.
 *
 * <p>
 * This listener listens for the {@link ApplicationReadyEvent} and on receiving
 * it:
 * </p>
 * <ul>
 * <li>Obtains the Logback {@link LoggerContext}</li>
 * <li>Initializes and starts the {@code MongoLogAppender}</li>
 * <li>Adds the appender to the root logger and to specified package
 * loggers</li>
 * </ul>
 *
 *
 * <p>
 * This setup ensures that logs from the application and selected packages are
 * asynchronously sent to MongoDB via the {@code MongoLogAppender}.
 * </p>
 */
@Component
@Slf4j
public class LogbackAppenderRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * The MongoDB log appender that handles log persistence.
     */
    private final MongoLogAppender mongoAppender;

    /**
     * List of package names to which the MongoLogAppender will be attached.
     */
    private static final String[] TARGET_LOG_PACKAGES = {
            "dev.Pedro",
            // "org.springframework.boot.actuate" // The root is getting the actuator logs
    };

    /**
     * Constructs a {@code LogbackAppenderRegistrar} with the given
     * {@link MongoLogAppender}.
     *
     * @param mongoAppender the MongoDB log appender to be registered with Logback
     *                      loggers
     */
    public LogbackAppenderRegistrar(MongoLogAppender mongoAppender) {
        this.mongoAppender = mongoAppender;
    }

    /**
     * Handles the application ready event by registering the
     * {@code MongoLogAppender}
     * with the Logback root logger and specified package loggers.
     *
     * @param event the application ready event
     */
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

        log.debug("Logback MongoAppenderRegistrar is ready");
    }
}