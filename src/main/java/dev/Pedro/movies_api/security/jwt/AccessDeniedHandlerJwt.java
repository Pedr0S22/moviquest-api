package dev.Pedro.movies_api.security.jwt;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.Pedro.movies_api.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom handler for forbidden access attempts (HTTP 403) in JWT-secured
 * endpoints.
 * <p>
 * Implements Spring Security's {@link AccessDeniedHandler} and is invoked
 * whenever
 * an authenticated user attempts to access a resource they do not have
 * permissions for.
 * Returns a structured JSON {@link ApiResponse} with details of the access
 * denial.
 * </p>
 */
@Component
@Slf4j
public class AccessDeniedHandlerJwt implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    /**
     * Constructs the handler with an {@link ObjectMapper} for JSON serialization.
     *
     * @param mapper the object mapper used to serialize the {@link ApiResponse} to
     *               JSON
     */
    public AccessDeniedHandlerJwt(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Handles access denied exceptions by returning a structured JSON response.
     *
     * @param request               the incoming {@link HttpServletRequest}
     * @param response              the outgoing {@link HttpServletResponse}
     * @param accessDeniedException the {@link AccessDeniedException} thrown by
     *                              Spring Security
     * @throws IOException      if an input/output error occurs during writing the
     *                          response
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error("Access denied: {}", accessDeniedException.getMessage());

        ApiResponse deniedResponse = new ApiResponse(
                HttpServletResponse.SC_FORBIDDEN,
                "Forbidden",
                "You do not have permission to access this resource.",
                request.getRequestURI());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        response.getWriter().write(mapper.writeValueAsString(deniedResponse));
    }

}
