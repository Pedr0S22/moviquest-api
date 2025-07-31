package dev.Pedro.movies_api.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import dev.Pedro.movies_api.model.LogEvent;

/**
 * Repository interface for accessing {@link LogEvent} documents in MongoDB.
 * <p>
 * Extends Spring Data's {@link MongoRepository} to provide CRUD operations
 * and additional MongoDB-specific functionalities for {@link LogEvent}
 * entities.
 * The primary key type is {@link ObjectId}.
 * </p>
 */
@Repository
public interface LogEventRepository extends MongoRepository<LogEvent, ObjectId> {

}
