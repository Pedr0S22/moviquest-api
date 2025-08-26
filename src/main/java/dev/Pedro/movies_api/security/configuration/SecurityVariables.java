package dev.Pedro.movies_api.security.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration class for loading security-related properties from the
 * application configuration.
 * <p>
 * Reads properties prefixed with {@code security}, such as JWT secret and
 * expiration time.
 * This allows the JWT settings to be easily configurable via
 * {@code application.properties}
 * or {@code application.yml}.
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "security")
@Data
public class SecurityVariables {

    private String jwtSecret;
    private long jwtExpirationMs;
}
