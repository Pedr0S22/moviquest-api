package dev.Pedro.movies_api.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.Pedro.movies_api.dto.request.LoginRequest;
import dev.Pedro.movies_api.dto.request.SignupRequest;
import dev.Pedro.movies_api.dto.response.JwtResponse;
import dev.Pedro.movies_api.exception.EmailAlreadyExistsException;
import dev.Pedro.movies_api.exception.RoleNotFoundException;
import dev.Pedro.movies_api.exception.UsernameAlreadyExistsException;
import dev.Pedro.movies_api.model.ClientRoles;
import dev.Pedro.movies_api.model.Role;
import dev.Pedro.movies_api.model.User;
import dev.Pedro.movies_api.repository.RoleRepository;
import dev.Pedro.movies_api.repository.UserRepository;
import dev.Pedro.movies_api.security.jwt.JwtUtils;
import dev.Pedro.movies_api.security.service.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling authentication and user registration.
 * <p>
 * Provides methods to handle user login and signup, including password
 * encoding,
 * role assignment, and JWT token generation.
 * </p>
 */
@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Constructs the authentication service with required dependencies.
     *
     * @param userRepository        repository to manage users
     * @param roleRepository        repository to manage roles
     * @param encoder               password encoder for storing hashed passwords
     * @param authenticationManager manager to authenticate login credentials
     * @param jwtUtils              utility to generate and validate JWT tokens
     */
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder,
            AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param login the {@link LoginRequest} containing username and password
     * @return a {@link JwtResponse} containing JWT token, user info, and roles
     */
    public JwtResponse handleLoginService(LoginRequest login) {

        // Authenticate the user with the provided username and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getUsername(),
                        login.getPassword()));

        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token based on the authentication
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Get user details from the authentication object
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Extract user roles into a list
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());

        JwtResponse response = new JwtResponse(HttpStatus.OK.value(),
                jwt,
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);

        return response;
    }

    /**
     * Registers a new user and assigns roles.
     *
     * @param signup the {@link SignupRequest} containing user info and optional
     *               roles
     * @return the saved {@link User} entity
     * @throws UsernameAlreadyExistsException if the username already exists
     * @throws EmailAlreadyExistsException    if the email already exists
     * @throws RoleNotFoundException          if any specified role does not exist
     *                                        in the database
     */
    public User handleSignupService(SignupRequest signup) {

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
            checkRoleExistenceAndAddRoles(ClientRoles.ROLE_USER, roles);
        } else {
            strRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        checkRoleExistenceAndAddRoles(ClientRoles.ROLE_ADMIN, roles);
                        break;
                    case "user":
                        checkRoleExistenceAndAddRoles(ClientRoles.ROLE_USER, roles);
                        break;
                    default:
                        log.debug("Role {} does not exist in database", role);
                        throw new RoleNotFoundException("Role " + role + " does not exist in database");
                }
            });
        }

        // Append roles into User object and send it to Database
        log.debug("the user {} has roles {}", username, roles);
        user.setRoles(roles);
        userRepository.save(user);

        return user;
    }

    /**
     * Checks if a role exists in the database and adds it to the user's roles set.
     * If the role is ADMIN, ensures that USER role is also added.
     *
     * @param clientRole the {@link ClientRoles} to check and add
     * @param roles      the set of roles to update
     * @throws RoleNotFoundException if the role does not exist in the database
     */
    private void checkRoleExistenceAndAddRoles(ClientRoles clientRole, Set<Role> roles) {
        Optional<Role> userRole = roleRepository.findByRoleName(clientRole);
        if (userRole.isPresent()) {
            roles.add(userRole.get());
            if (clientRole == ClientRoles.ROLE_ADMIN) {
                addRoleIfMissing(ClientRoles.ROLE_USER, roles);
            }
        } else {
            log.debug("Role {} does not exist in database", userRole);
            throw new RoleNotFoundException("Role " + userRole + " does not exist in database");
        }
    }

    /**
     * Adds a role to the roles set if it is not already present.
     *
     * @param clientRole the {@link ClientRoles} to add
     * @param roles      the set of roles to update
     * @throws RoleNotFoundException if the role does not exist in the database
     */
    private void addRoleIfMissing(ClientRoles clientRole, Set<Role> roles) {

        boolean hasRole = roles.stream().anyMatch(role -> role.getRoleName().equals(clientRole));

        if (!hasRole) {
            Optional<Role> userRole = roleRepository.findByRoleName(clientRole);
            if (userRole.isPresent()) {
                roles.add(userRole.get());
            } else {
                log.debug("Role {} does not exist in database", userRole);
                throw new RoleNotFoundException("Role " + userRole + " does not exist in database");
            }
        }
    }
}
