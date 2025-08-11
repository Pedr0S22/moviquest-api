package dev.Pedro.movies_api.security.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "security")
@Data
public class SecurityVariables {

    private String jwtSecret;
    private long jwtExpirationMs;
}
