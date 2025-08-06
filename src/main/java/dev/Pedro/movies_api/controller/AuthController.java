package dev.Pedro.movies_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.Pedro.movies_api.security.request.LoginRequest;
import dev.Pedro.movies_api.security.request.SignupRequest;
import dev.Pedro.movies_api.service.AuthService;
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
    public ResponseEntity<Object> handleLogin(@RequestBody LoginRequest login) {

        // Se o login corresponder enviar JwtResponse

        return ResponseEntity.ok(null);
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> handleSignup(@RequestBody SignupRequest signup) {

        // verificar se já existe username

        // verificar se já existe email

        // Criar objeto User e Role e inserir na base de dados
        // encode da password

        // Enviar a confirmação de criação da conta

        return ResponseEntity.ok(null);
    }
}
