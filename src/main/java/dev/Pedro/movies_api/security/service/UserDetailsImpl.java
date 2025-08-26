package dev.Pedro.movies_api.security.service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.Pedro.movies_api.model.User;
import lombok.AllArgsConstructor;

/**
 * Implementation of Spring Security's {@link UserDetails} interface.
 * <p>
 * Represents an authenticated user with their username, email, password, and
 * authorities (roles).
 * This class is used by Spring Security for authentication and authorization
 * checks.
 * </p>
 */
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private ObjectId id;
    private String username;
    private String email;

    @JsonIgnore // prevents passowrd serialization
    private String password;

    private Collection<? extends GrantedAuthority> authorities; // roles

    /**
     * Builds a {@link UserDetailsImpl} object from a {@link User} entity.
     * <p>
     * Maps the user's roles to Spring Security {@link GrantedAuthority}.
     * </p>
     *
     * @param user the {@link User} entity
     * @return a new {@link UserDetailsImpl} instance
     */
    public static UserDetailsImpl build(User user) {
        // Map the roles of the user to GrantedAuthority
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .collect(Collectors.toList());

        // Return a new UserDetailsImpl object
        return new UserDetailsImpl(
                user.getId(), // User ID
                user.getUsername(), // Username
                user.getEmail(), // Email
                user.getPassword(), // Password
                authorities); // User authorities
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public ObjectId getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account is not expired
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Account is not locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials are not expired
    }

    @Override
    public boolean isEnabled() {
        return true; // Account is enabled
    }

    /**
     * Compares two {@link UserDetailsImpl} objects based on user ID.
     *
     * @param obj the other object to compare
     * @return {@code true} if the IDs are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) obj;
        return Objects.equals(id, user.id);
    }

}
