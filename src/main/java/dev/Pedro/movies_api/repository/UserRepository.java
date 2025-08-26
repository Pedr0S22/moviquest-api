package dev.Pedro.movies_api.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import dev.Pedro.movies_api.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for managing {@link User} entities in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide standard CRUD operations and
 * includes custom query methods for user lookups by username and email.
 * </p>
 */
@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {

    /**
     * Finds a user by their username.
     *
     * @param username the unique username of the user
     * @return an {@link Optional} containing the {@link User} if found, or empty if
     *         not
     */
    public Optional<User> findByUsername(String username);

    /**
     * Checks whether a user exists with the given username.
     *
     * @param username the username to check
     * @return {@code true} if a user with the given username exists, {@code false}
     *         otherwise
     */
    public Boolean existsByUsername(String username);

    /**
     * Checks whether a user exists with the given email.
     *
     * @param email the email to check
     * @return {@code true} if a user with the given email exists, {@code false}
     *         otherwise
     */
    public Boolean existsByEmail(String email);
}