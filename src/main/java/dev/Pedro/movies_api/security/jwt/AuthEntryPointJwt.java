package dev.Pedro.movies_api.security.jwt;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.Pedro.movies_api.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom entry point for handling unauthorized access (HTTP 401) in JWT-secured
 * endpoints.
 * <p>
 * Implements Spring Security's {@link AuthenticationEntryPoint} and is invoked
 * whenever
 * an unauthenticated user attempts to access a protected resource. Returns a
 * structured
 * JSON {@link ApiResponse} describing the error.
 * </p>
 */
@Component
@Slf4j
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    /**
     * Constructs the entry point with an {@link ObjectMapper} for JSON
     * serialization.
     *
     * @param mapper the object mapper used to serialize the {@link ApiResponse} to
     *               JSON
     */
    public AuthEntryPointJwt(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Handles authentication exceptions by returning a structured JSON response.
     *
     * @param request       the incoming {@link HttpServletRequest}
     * @param response      the outgoing {@link HttpServletResponse}
     * @param authException the {@link AuthenticationException} thrown by Spring
     *                      Security
     * @throws IOException      if an input/output error occurs during writing the
     *                          response
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.error("Unauthorized error: {}", authException.getMessage());

        ApiResponse unauthorizedResponse = new ApiResponse(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                authException.getMessage(),
                request.getRequestURI());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(mapper.writeValueAsString(unauthorizedResponse));
    }

}
