package dev.Pedro.movies_api.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import dev.Pedro.movies_api.model.Movie;

/**
 * Repository interface for managing {@link Movie} entities in MongoDB.
 * <p>
 * Extends {@link MongoRepository} to provide CRUD operations and adds
 * custom query methods for movie lookups by imdbId.
 * </p>
 */
@Repository
public interface MovieRepository extends MongoRepository<Movie, ObjectId> {

    /**
     * Finds a movie by its imdbId ID.
     *
     * @param imdbId the imdbId identifier of the movie
     * @return an {@link Optional} containing the {@link Movie} if found, or empty
     *         if not
     */
    public Optional<Movie> findMovieByImdbId(String imdbId);

    /**
     * Deletes a movie by its imdbId.
     *
     * @param imdbId the imdbId identifier of the movie to delete
     */
    public void deleteByImdbId(String imdbId);

    /**
     * Checks whether a movie exists with the given imdbId.
     *
     * @param imdbId the imdbId identifier of the movie
     * @return {@code true} if a movie with the given imdbId exists, {@code false}
     *         otherwise
     */
    public boolean existsByImdbId(String imdbId);
}
