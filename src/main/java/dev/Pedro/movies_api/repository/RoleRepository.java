package dev.Pedro.movies_api.repository;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import dev.Pedro.movies_api.model.ClientRoles;
import dev.Pedro.movies_api.model.Role;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

/**
 * Repository interface for managing {@link Role} entities in MongoDB.
 * <p>
 * Provides CRUD operations through {@link MongoRepository} and includes
 * a custom method for finding roles by their {@link ClientRoles} enum value.
 * </p>
 */
@Repository
public interface RoleRepository extends MongoRepository<Role, ObjectId> {

    /**
     * Finds a {@link Role} by its role name (defined in {@link ClientRoles}).
     *
     * @param roleName the enum value representing the role (e.g., ADMIN, USER)
     * @return an {@link Optional} containing the {@link Role} if found, or empty if
     *         not
     */
    public Optional<Role> findByRoleName(ClientRoles roleName);

}
