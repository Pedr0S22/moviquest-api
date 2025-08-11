package dev.Pedro.movies_api.repository;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import dev.Pedro.movies_api.model.ClientRoles;
import dev.Pedro.movies_api.model.Role;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<Role, ObjectId> {

    public Optional<Role> findByRoleName(ClientRoles roleName);

}
