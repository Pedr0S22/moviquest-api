package dev.Pedro.movies_api.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import dev.Pedro.movies_api.exception.EmailAlreadyExistsException;
import dev.Pedro.movies_api.exception.RoleNotFoundException;
import dev.Pedro.movies_api.exception.UsernameAlreadyExistsException;
import dev.Pedro.movies_api.model.ClientRoles;
import dev.Pedro.movies_api.model.Role;
import dev.Pedro.movies_api.model.User;
import dev.Pedro.movies_api.repository.RoleRepository;
import dev.Pedro.movies_api.repository.UserRepository;
import dev.Pedro.movies_api.security.request.LoginRequest;
import dev.Pedro.movies_api.security.request.SignupRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    public ResponseEntity<Object> handleLoginService(@Valid @RequestBody LoginRequest login) {

        // fazer autenticação

        // Verificar na base de dados se está tudo correto

        // Atribuir o token se sim e enviar responseBody
        return ResponseEntity.ok(null);
    }

    public User handleSignupService(@Valid @RequestBody SignupRequest signup) {

        // Verify if username exists in database
        String username = signup.getUsername();
        if (userRepository.existsByUsername(username)) {
            log.debug("The username {} is already taken", username);
            throw new UsernameAlreadyExistsException("The username is already taken");
        }

        // verify if email exists in database
        String email = signup.getEmail();
        if (userRepository.existsByEmail(email)) {
            log.debug("The email {} is already taken", email);
            throw new EmailAlreadyExistsException("The email is already taken");
        }

        // User object creation with encoded password
        User user = new User(username,
                email,
                encoder.encode(signup.getPassword()));

        // Verification of the existence of client roles
        Set<String> strRoles = signup.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Optional<Role> userRole = roleRepository.findByRoleName(ClientRoles.ROLE_USER);
            if (userRole.isPresent()) {
                roles.add(userRole.get());
            } else {
                log.debug("Role {} does not exist in database", userRole);
                throw new RoleNotFoundException("Role " + userRole + " does not exist in database");
            }
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        Optional<Role> adminRole = roleRepository.findByRoleName(ClientRoles.ROLE_ADMIN);
                        if (adminRole.isPresent()) {
                            roles.add(adminRole.get());
                            roles.add(new Role(ClientRoles.ROLE_USER));
                        } else {
                            log.debug("Role {} does not exist in database", adminRole);
                            throw new RoleNotFoundException("Role " + adminRole + " does not exist in database");
                        }
                        break;

                    default:
                        Optional<Role> userRole = roleRepository.findByRoleName(ClientRoles.ROLE_USER);
                        if (userRole.isPresent()) {
                            log.debug("the userRole: {}", userRole);
                            roles.add(userRole.get());
                        } else {
                            log.debug("Role {} does not exist in database", userRole);
                            throw new RoleNotFoundException("Role " + userRole + " does not exist in database");
                        }
                        break;
                }
            });
        }

        // Append roles into User object and send it to Database
        user.setRoles(roles);
        userRepository.save(user);

        return user;
    }
}
