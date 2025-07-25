package dev.Pedro.movies_api.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import dev.Pedro.movies_api.model.LogEvent;

@Repository
public interface LogEventRepository extends MongoRepository<LogEvent, ObjectId> {

}
