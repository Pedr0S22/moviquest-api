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

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> handleLogin(@Valid @RequestBody LoginRequest login) {

        log.info("Received request to login the user {} ", login.getUsername());
        JwtResponse response = authService.handleLoginService(login);
        log.info("Login of user {} was successful", login.getUsername());

        return ResponseEntity.ok(response);
    }

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
