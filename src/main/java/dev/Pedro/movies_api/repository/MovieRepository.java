package dev.Pedro.movies_api.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import dev.Pedro.movies_api.model.Movie;

@Repository
public interface MovieRepository extends MongoRepository<Movie, ObjectId> {
    public Optional<Movie> findMovieByImdbId(String imdbId);

    public void deleteByImdbId(String imdbId);
}
