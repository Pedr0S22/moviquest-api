package dev.Pedro.movies_api.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import dev.Pedro.movies_api.dto.request.NewMovieRequest;
import dev.Pedro.movies_api.dto.request.SearchMoviesRequest;
import dev.Pedro.movies_api.dto.request.UpdateMovieRequest;
import dev.Pedro.movies_api.dto.response.ApiResponse;
import dev.Pedro.movies_api.exception.MovieAlreadyExistsException;
import dev.Pedro.movies_api.exception.MovieNotFoundException;
import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.repository.MovieRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class responsible for managing {@link Movie} entities.
 * <p>
 * Provides CRUD operations, search functionality, and validation.
 */
@Service
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * Constructs the MovieService with required dependencies.
     *
     * @param movieRepository repository for CRUD operations on movies
     * @param mongoTemplate   template for advanced MongoDB queries
     */
    public MovieService(MovieRepository movieRepository, MongoTemplate mongoTemplate) {
        this.movieRepository = movieRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Retrieves all movies from the database.
     *
     * @return a list of all {@link Movie} objects
     */
    public List<Movie> AllMovies() {
        return movieRepository.findAll();
    }

    /**
     * Retrieves a single movie by its imdbId.
     *
     * @param imdbId the imdbId of the movie
     * @return the matching {@link Movie}
     * @throws MovieNotFoundException if the movie does not exist
     */
    public Movie singleMovie(String imdbId) {

        Optional<Movie> movie = movieRepository.findMovieByImdbId(imdbId);
        if (movie.isPresent()) {
            return movie.get();
        } else {

            String errorMessage = "The movie with imdbId " + imdbId + " does not exist";
            log.debug(errorMessage);

            throw new MovieNotFoundException(errorMessage);
        }
    }

    /**
     * Searches for movies based on the provided filters.
     * <p>
     * Supports filtering by title (case-insensitive), genres, and release date
     * range.
     *
     * @param search the search criteria
     * @return a list of matching {@link Movie} objects
     */
    public List<Movie> searchMovies(SearchMoviesRequest search) {

        Query query = new Query();
        List<Criteria> criteria = new ArrayList<>();
        List<Movie> movies;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // fields
        String title = search.getTitle();
        Set<String> genres = search.getGenres();
        Date releaseDateAfter = search.getReleaseDateAfter();
        Date releaseDateBefore = search.getReleaseDateBefore();

        if (title != null)
            criteria.add(Criteria.where("title").regex(title, "i"));

        if (genres != null && !genres.isEmpty())
            criteria.add(Criteria.where("genres").in(genres));

        if (releaseDateAfter != null && releaseDateBefore != null) {
            criteria.add(
                    Criteria.where("releaseDate")
                            .gte(formatter.format(releaseDateAfter))
                            .lte(formatter.format(releaseDateBefore)));

        } else if (releaseDateAfter != null || releaseDateBefore != null) {

            if (releaseDateAfter == null)
                criteria.add(Criteria.where("releaseDate")
                        .lte(formatter.format(releaseDateBefore)));
            else
                criteria.add(Criteria.where("releaseDate")
                        .gte(formatter.format(releaseDateAfter)));
        }

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        else
            log.info("No filters - returning all movies");
        movies = movieRepository.findAll();

        log.debug("Final query: {}", query.getQueryObject().toJson());

        movies = mongoTemplate.find(query, Movie.class);

        return movies;
    }

    /**
     * Deletes a movie by imdbId.
     *
     * @param imdbId  the imdbId of the movie
     * @param request the current HTTP request, used for URI in the response
     * @return an {@link ApiResponse} indicating success
     * @throws MovieNotFoundException if the movie does not exist
     */
    public ApiResponse deleteMovieByImdbId(String imdbId, HttpServletRequest request) {

        if (!verifyMovieExistence(imdbId)) {
            throw new MovieNotFoundException(
                    "The movie with imdbId " + imdbId + " was does not exist. Impossible to delete");
        }

        movieRepository.deleteByImdbId(imdbId);

        String message = "The movie with imdbId " + imdbId + " was deleted";
        ApiResponse response = new ApiResponse(HttpStatus.OK.value(), message, request.getRequestURI());

        return response;
    }

    /**
     * Saves a new movie in the database.
     *
     * @param newMovie the movie details to save
     * @return the saved {@link Movie}
     * @throws MovieAlreadyExistsException if a movie with the same imdbId already
     *                                     exists
     */
    public Movie saveMovie(NewMovieRequest newMovie) {
        if (verifyMovieExistence(newMovie.getImdbId())) {
            throw new MovieAlreadyExistsException(
                    "The movie with imdbId " + newMovie.getImdbId()
                            + " already exists. Impossible to insert this new Movie");
        }
        Movie movie = new Movie(new ObjectId(), newMovie.getImdbId(), newMovie.getTitle(),
                newMovie.getReleaseDate().toString(), newMovie.getTrailerLink(), newMovie.getPoster(),
                newMovie.getGenres(), newMovie.getBackdrops(), null);

        Movie movieInserted = movieRepository.save(movie);

        return movieInserted;
    }

    /**
     * Updates an existing movie by imdbId.
     * <p>
     * Only fields that are not null in {@link UpdateMovieRequest} will be updated.
     *
     * @param imdbId   the imdbId of the movie to update
     * @param updMovie the fields to update
     * @return the updated {@link Movie}
     * @throws MovieNotFoundException if the movie does not exist
     */
    public Movie updateMovie(String imdbId, UpdateMovieRequest updMovie) {

        Movie existingMovie = singleMovie(imdbId);

        if (updMovie.getTitle() != null)
            existingMovie.setTitle(updMovie.getTitle());

        if (updMovie.getReleaseDate() != null)
            existingMovie.setReleaseDate(updMovie.getReleaseDate().toString());

        if (updMovie.getTrailerLink() != null)
            existingMovie.setTrailerLink(updMovie.getTrailerLink());

        if (updMovie.getPoster() != null)
            existingMovie.setPoster(updMovie.getPoster());

        if (updMovie.getGenres() != null)
            existingMovie.setGenres(updMovie.getGenres());

        if (updMovie.getBackdrops() != null)
            existingMovie.setBackdrops(updMovie.getBackdrops());

        Movie movie = movieRepository.save(existingMovie);

        return movie;
    }

    /**
     * Checks whether a movie with the given imdbId exists.
     *
     * @param imdbId the imdbId to check
     * @return true if the movie exists, false otherwise
     */
    public boolean verifyMovieExistence(String imdbId) {
        return movieRepository.existsByImdbId(imdbId);
    }
}