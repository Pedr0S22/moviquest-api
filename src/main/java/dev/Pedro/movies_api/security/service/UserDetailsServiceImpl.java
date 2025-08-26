package dev.Pedro.movies_api.security.service;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.Pedro.movies_api.model.User;
import dev.Pedro.movies_api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of Spring Security's {@link UserDetailsService}.
 * <p>
 * Loads user-specific data during authentication. Delegates to
 * {@link UserRepository}
 * to fetch a {@link User} entity by username and builds a
 * {@link UserDetailsImpl}
 * object required by Spring Security.
 * </p>
 */
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructs the service with a {@link UserRepository}.
     *
     * @param userRepository the repository used to fetch users by username
     */
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username for authentication.
     * <p>
     * Queries the {@link UserRepository} to find the user. If the user exists,
     * a {@link UserDetailsImpl} is returned. If not, a
     * {@link UsernameNotFoundException}
     * is thrown and a debug message is logged.
     * </p>
     *
     * @param username the username of the user to load
     * @return the {@link UserDetails} object representing the authenticated user
     * @throws UsernameNotFoundException if the user does not exist
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent()) {
            return UserDetailsImpl.build(user.get());
        } else {
            log.debug("The User with username {} does not exist", username);

            throw new UsernameNotFoundException("The User with username " + username + " does not exist");
        }

    }

}
