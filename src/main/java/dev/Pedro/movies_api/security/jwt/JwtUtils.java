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

@Component
@Slf4j
public class JwtUtils {

    private final String jwtSecret;
    private final long jwtExpirationMs;

    public JwtUtils(SecurityVariables jwtVars) {
        this.jwtSecret = jwtVars.getJwtSecret();
        this.jwtExpirationMs = jwtVars.getJwtExpirationMs();
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject((userPrincipal.getUsername()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    private SecretKey key() {
        // Decode the JWT secret and create a signing key
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        // Parse the JWT token and return the subject (username)
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

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
