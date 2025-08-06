package dev.Pedro.movies_api.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import dev.Pedro.movies_api.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {

    public Optional<User> findUserbyUsername(String username);

}