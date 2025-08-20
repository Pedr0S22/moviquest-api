package dev.Pedro.movies_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

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

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signup;
    private LoginRequest login;

    @BeforeEach
    private void setup() {
        login = new LoginRequest("john", "password");
        signup = new SignupRequest("john", "john@mail.com", "password",
                Set.of("user"));
    }

    @Test
    void testLoginServiceSuccess() {

        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(new ObjectId(), "john", "john@mail.com", "hashedpass",
                Set.of());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("fake-jwt");

        JwtResponse response = authService.handleLoginService(login);

        assertEquals("fake-jwt", response.getAccessToken());
        assertEquals("john", response.getUsername());
        assertEquals("john@mail.com", response.getEmail());
    }

    @Test
    void testSignupServiceSuccess() {

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");

        Role userRole = new Role(new ObjectId(), ClientRoles.ROLE_USER);

        when(roleRepository.findByRoleName(ClientRoles.ROLE_USER)).thenReturn(Optional.of(userRole));

        User savedUser = new User("john", "john@mail.com", "encodedPass");

        savedUser.setRoles(Set.of(userRole));

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.handleSignupService(signup);

        assertEquals("john", result.getUsername());
        assertEquals("john@mail.com", result.getEmail());
        assertTrue(result.getRoles().contains(userRole));
    }

    @Test
    void testRoleNotFound() {
        SignupRequest signup1 = new SignupRequest("john", "john@mail.com", "password", Set.of("manager"));

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");

        assertThrows(RoleNotFoundException.class, () -> authService.handleSignupService(signup1));
    }

    @Test
    void testUsernameAlreadyExists() {

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> authService.handleSignupService(signup));

    }

    @Test
    void testEmailAlreadyExists() {

        when(userRepository.existsByEmail("john@mail.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.handleSignupService(signup));
    }

    @Test
    void testAdminRoleAddsUserRolePrivateMethod() {
        SignupRequest signupAdmin = new SignupRequest("admin", "admin@mail.com", "password", Set.of("admin"));

        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(userRepository.existsByEmail("admin@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPass");

        Role adminRole = new Role(new ObjectId(), ClientRoles.ROLE_ADMIN);
        Role userRole = new Role(new ObjectId(), ClientRoles.ROLE_USER);

        when(roleRepository.findByRoleName(ClientRoles.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByRoleName(ClientRoles.ROLE_USER)).thenReturn(Optional.of(userRole));

        User savedUser = new User("admin", "admin@mail.com", "encodedPass");
        savedUser.setRoles(Set.of(adminRole, userRole));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.handleSignupService(signupAdmin);

        assertTrue(result.getRoles().contains(adminRole));
        assertTrue(result.getRoles().contains(userRole));
    }

}
