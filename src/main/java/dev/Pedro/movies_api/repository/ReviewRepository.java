package dev.Pedro.movies_api.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import dev.Pedro.movies_api.model.Review;

/**
 * Repository interface for managing {@link Review} entities in MongoDB.
 * <p>
 * Inherits standard CRUD operations from
 * {@link MongoRepository}. Custom query methods can be added here if needed.
 * </p>
 */
@Repository
public interface ReviewRepository extends MongoRepository<Review, ObjectId> {

}
