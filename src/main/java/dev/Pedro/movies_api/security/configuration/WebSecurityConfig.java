package dev.Pedro.movies_api.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import dev.Pedro.movies_api.security.jwt.AccessDeniedHandlerJwt;
import dev.Pedro.movies_api.security.jwt.AuthEntryPointJwt;
import dev.Pedro.movies_api.security.jwt.AuthTokenFilter;
import dev.Pedro.movies_api.security.jwt.JwtUtils;
import dev.Pedro.movies_api.security.service.UserDetailsServiceImpl;

/**
 * Configuration class for Spring Security.
 * <p>
 * Sets up authentication, authorization, JWT token filtering, password
 * encoding,
 * and session management. Enables method-level security annotations with
 * {@code @EnableMethodSecurity}.
 * </p>
 */
@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt entryPointJwt;
    private final AccessDeniedHandlerJwt accessDeniedJwt;
    private final JwtUtils jwtUtils;

    /**
     * Constructs the security configuration with required dependencies.
     *
     * @param userDetailsService the service used to load user details for
     *                           authentication
     * @param entryPointJwt      handles unauthorized access attempts
     * @param accessDeniedJwt    handles forbidden access attempts
     * @param jwtUtils           utility class for generating and validating JWT
     *                           tokens
     */
    public WebSecurityConfig(UserDetailsServiceImpl userDetailsService, AuthEntryPointJwt entryPointJwt,
            AccessDeniedHandlerJwt accessDeniedJwt, JwtUtils jwtUtils) {

        this.userDetailsService = userDetailsService;
        this.entryPointJwt = entryPointJwt;
        this.accessDeniedJwt = accessDeniedJwt;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Bean for the JWT authentication token filter.
     * <p>
     * Intercepts requests to validate JWT tokens and authenticate users.
     * </p>
     *
     * @return a configured {@link AuthTokenFilter} bean
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils, userDetailsService);
    }

    /**
     * Exposes the {@link AuthenticationManager} bean for authentication purposes.
     *
     * @param authConfig the authentication configuration
     * @return the authentication manager
     * @throws Exception if the authentication manager cannot be built
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Bean for password encoding.
     * <p>
     * Uses {@link BCryptPasswordEncoder} to hash passwords securely.
     * </p>
     *
     * @return a {@link PasswordEncoder} bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the HTTP security filter chain.
     * <p>
     * Disables CSRF, sets session management to stateless, configures exception
     * handling for unauthorized and forbidden requests, sets route-based
     * authorization rules, and adds the JWT token filter.
     * </p>
     *
     * @param http the {@link HttpSecurity} object to configure
     * @return a built {@link SecurityFilterChain} bean
     * @throws Exception if there is a configuration error
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configure CSRF protection, exception handling, session management, and
        // authorization
        http.csrf(csrf -> csrf.disable()) // Disable CSRF protection
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(entryPointJwt)
                        .accessDeniedHandler(accessDeniedJwt))
                // Set unauthorized handler
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Set session policy to stateless
                .authorizeHttpRequests(auth -> auth
                        // Configure authorization for HTTP requests
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/movies/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/logging/**").hasRole("ADMIN")
                        .anyRequest().authenticated());

        // Add the JWT token filter before the username/password authentication filter
        http.addFilterBefore(authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Build and return the security filter chain
    }
}
