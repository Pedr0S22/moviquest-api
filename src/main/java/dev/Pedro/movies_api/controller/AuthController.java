package dev.Pedro.movies_api.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.Pedro.movies_api.dto.request.LoginRequest;
import dev.Pedro.movies_api.dto.request.SignupRequest;
import dev.Pedro.movies_api.dto.response.JwtResponse;
import dev.Pedro.movies_api.model.User;
import dev.Pedro.movies_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller that handles authentication-related endpoints such as login and
 * signup.
 * <p>
 * It delegates the actual authentication logic to the {@link AuthService} and
 * builds appropriate responses for the client.
 * </p>
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Creates a new {@code AuthController} with the required authentication
     * service.
     *
     * @param authService the service responsible for handling authentication and
     *                    registration
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles user login requests.
     * <p>
     * Validates the given login request, delegates authentication to
     * {@link AuthService},
     * and returns a {@link JwtResponse} containing the JWT token and user details
     * if authentication is successful.
     * </p>
     *
     * @param login the login request containing username and password
     * @return a {@code ResponseEntity} containing the {@link JwtResponse} with JWT
     *         details
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> handleLogin(@Valid @RequestBody LoginRequest login) {

        log.info("Received request to login the user {} ", login.getUsername());
        JwtResponse response = authService.handleLoginService(login);
        log.info("Login of user {} was successful", login.getUsername());

        return ResponseEntity.ok(response);
    }

    /**
     * Handles user signup (registration) requests.
     * <p>
     * Validates the signup request, registers the user through {@link AuthService},
     * and constructs a {@link JwtResponse} containing the new user's details,
     * roles,
     * and a success message.
     * </p>
     *
     * @param signup the signup request containing user registration data
     * @return a {@code ResponseEntity} containing the {@link JwtResponse} with
     *         registration details,
     *         returned with HTTP status {@code 201 CREATED}
     */
    @PostMapping("/signup")
    public ResponseEntity<JwtResponse> handleSignup(@Valid @RequestBody SignupRequest signup) {

        log.info("Received request to register a new User");
        User user = authService.handleSignupService(signup);
        log.info("User {} registered successfully!", user.getUsername());

        String message = "User " + user.getUsername() + " registered successfully!";

        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getRoleName().toString().substring(12))
                .collect(Collectors.toSet());

        JwtResponse signupResponse = new JwtResponse(
                HttpStatus.CREATED.value(),
                user.getUsername(),
                user.getEmail(),
                roles,
                message);

        return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
    }
}
