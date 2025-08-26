package dev.Pedro.movies_api.security.jwt;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.Pedro.movies_api.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT authentication filter that intercepts every HTTP request once.
 * <p>
 * Extends {@link OncePerRequestFilter} to extract and validate JWT tokens
 * from the {@code Authorization} header. If a valid token is found, the
 * user's authentication is set in the {@link SecurityContextHolder}.
 * </p>
 */
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Constructs the filter with required dependencies.
     *
     * @param jwtUtils           utility for JWT token operations (validation,
     *                           parsing)
     * @param userDetailsService service to load user details by username
     */
    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Filters each HTTP request to validate JWT tokens and set authentication.
     * <p>
     * If a valid JWT is present in the {@code Authorization} header, it extracts
     * the username, loads the user details, creates a
     * {@link UsernamePasswordAuthenticationToken},
     * and sets it in the {@link SecurityContextHolder}.
     * </p>
     *
     * @param request     the incoming {@link HttpServletRequest}
     * @param response    the outgoing {@link HttpServletResponse}
     * @param filterChain the filter chain to pass control to the next filter
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null,
                        userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Parses the JWT from the {@code Authorization} header of the request.
     *
     * @param request the incoming HTTP request
     * @return the JWT string if present and valid, otherwise {@code null}
     */
    private String parseJwt(HttpServletRequest request) {

        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

}
