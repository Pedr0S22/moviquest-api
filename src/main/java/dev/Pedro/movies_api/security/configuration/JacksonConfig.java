package dev.Pedro.movies_api.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

/**
 * Configuration class for customizing Jackson's JSON serialization and
 * deserialization.
 * <p>
 * Registers the {@link JavaTimeModule} to properly handle Java 8 date and time
 * types
 * (e.g., {@link java.time.LocalDate}, {@link java.time.LocalDateTime}) and
 * disables
 * serialization of dates as timestamps, so they are output in ISO-8601 format.
 * </p>
 */
@Configuration
public class JacksonConfig {

    /**
     * Configures and returns a custom {@link ObjectMapper} for JSON processing.
     * <p>
     * This mapper supports Java 8 time types and produces readable ISO-8601 date
     * strings
     * instead of numeric timestamps.
     * </p>
     *
     * @return a configured {@link ObjectMapper} bean
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}