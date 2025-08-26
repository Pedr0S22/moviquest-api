package dev.Pedro.movies_api.security.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import dev.Pedro.movies_api.security.configuration.SecurityVariables;
import dev.Pedro.movies_api.security.service.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating, parsing, and validating JWT tokens.
 * <p>
 * Provides methods to create JWTs with expiration, extract the username from a
 * token,
 * and validate a token's signature and structure.
 * </p>
 */
@Component
@Slf4j
public class JwtUtils {

    private final String jwtSecret;
    private final long jwtExpirationMs;

    /**
     * Constructs the utility with security configuration variables.
     *
     * @param jwtVars the {@link SecurityVariables} containing the JWT secret and
     *                expiration time
     */
    public JwtUtils(SecurityVariables jwtVars) {
        this.jwtSecret = jwtVars.getJwtSecret();
        this.jwtExpirationMs = jwtVars.getJwtExpirationMs();
    }

    /**
     * Generates a JWT token for a given authenticated user.
     *
     * @param authentication the {@link Authentication} object containing the user
     *                       details
     * @return a signed JWT string with subject, issued date, and expiration
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject((userPrincipal.getUsername()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    /**
     * Builds a {@link SecretKey} from the configured JWT secret.
     *
     * @return the secret key used for signing and verifying JWT tokens
     */
    private SecretKey key() {
        // Decode the JWT secret and create a signing key
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT token string
     * @return the username contained in the token's subject
     */
    public String getUserNameFromJwtToken(String token) {
        // Parse the JWT token and return the subject (username)
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates a JWT token.
     * <p>
     * Checks the token's signature, expiration, and structure.
     * Logs errors for invalid, expired, unsupported, or empty tokens.
     * </p>
     *
     * @param authToken the JWT token string to validate
     * @return {@code true} if the token is valid, {@code false} otherwise
     */
    public boolean validateJwtToken(String authToken) {
        try {
            // Parse the token and verify its signature
            Jwts.parser().verifyWith(key()).build().parse(authToken);
            return true; // Token is valid
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage()); // Log invalid token error
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage()); // Log expired token error
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage()); // Log unsupported token error
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage()); // Log empty claims error
        }

        return false; // Token is invalid
    }

}
