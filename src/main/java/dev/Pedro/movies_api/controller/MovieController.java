package dev.Pedro.movies_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.Pedro.movies_api.dto.request.NewMovieRequest;
import dev.Pedro.movies_api.dto.request.SearchMoviesRequest;
import dev.Pedro.movies_api.dto.request.UpdateMovieRequest;
import dev.Pedro.movies_api.dto.response.ApiResponse;
import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Controller that manages movie-related operations.
 * <p>
 * Provides endpoints for retrieving, searching, creating, updating,
 * and deleting movies. Some endpoints are restricted to users with
 * the {@code ADMIN} role.
 * </p>
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    /**
     * Creates a new {@code MovieController} with the required movie service.
     *
     * @param movieService the service responsible for handling movie operations
     */
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * Retrieves all movies in the database.
     *
     * @return a {@code ResponseEntity} containing a list of all {@link Movie}
     *         objects
     */
    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        log.info("Received request to GET all movies.");
        List<Movie> movies = movieService.AllMovies();
        log.info("Returning {} movies.", movies.size());

        return ResponseEntity.ok(movies);
    }

    /**
     * Retrieves a single movie by its ImdbId.
     *
     * @param imdbId the unique IMDb identifier of the movie
     * @return a {@code ResponseEntity} containing the requested {@link Movie}
     */
    @GetMapping("/{imdbId}")
    public ResponseEntity<Movie> getMovie(@PathVariable String imdbId) {
        log.info("Received request to GET the movie with imdbId {}", imdbId);
        Movie movie = movieService.singleMovie(imdbId);
        log.info("The movie with imdbId {} was provided", imdbId);

        return ResponseEntity.ok(movie);
    }

    /**
     * Searches for movies based on filter criteria such as title, genres,
     * and release date range.
     *
     * @param search the search request containing filter parameters
     * @return a {@code ResponseEntity} containing the list of matching
     *         {@link Movie} objects
     */
    @PostMapping("/search")
    public ResponseEntity<List<Movie>> getMovies(@Valid @RequestBody SearchMoviesRequest search) {

        log.info("Received request to SEARCH movies with filters: {}, {}, {}, {}", search.getTitle(),
                search.getGenres(), search.getReleaseDateAfter(), search.getReleaseDateBefore());

        List<Movie> movies = movieService.searchMovies(search);
        log.info("Returning {} movies.", movies.size());

        return ResponseEntity.ok(movies);
    }

    /**
     * Deletes a movie by its imdbId .
     * <p>
     * Only accessible to users with the {@code ADMIN} role.
     * Returns an {@link ApiResponse} with the result of the operation.
     * </p>
     *
     * @param imdbId  the imdbId identifier of the movie to delete
     * @param request the HTTP request (used for logging/auditing purposes)
     * @return a {@code ResponseEntity} containing the {@link ApiResponse} result
     */
    @DeleteMapping("/delete/{imdbId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteMovie(@PathVariable String imdbId, HttpServletRequest request) {

        log.info("Received request to DELETE the movie with imdbId {}", imdbId);
        ApiResponse deleteResponse = movieService.deleteMovieByImdbId(imdbId, request);
        log.info("The movie with imdbId {} was deleted successfully", imdbId);

        return ResponseEntity.ok(deleteResponse);
    }

    /**
     * Inserts a new movie into the database.
     * <p>
     * Only accessible to users with the {@code ADMIN} role.
     * </p>
     *
     * @param newMovie the request containing details of the new movie
     * @return a {@code ResponseEntity} containing the created {@link Movie},
     *         returned with HTTP status {@code 201 CREATED}
     */
    @PostMapping("/newMovie")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Movie> insertMovie(@Valid @RequestBody NewMovieRequest newMovie) {

        log.info("Received request to INSERT a new movie with imdbId {}", newMovie.getImdbId());
        Movie movie = movieService.saveMovie(newMovie);
        log.info("The movie with imdbId {} was inserted successfully", movie.getImdbId());

        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }

    /**
     * Updates an existing movie in the database.
     * <p>
     * Only accessible to users with the {@code ADMIN} role.
     * </p>
     *
     * @param imdbId   the imdbId identifier of the movie to update
     * @param updMovie the request containing updated movie details
     * @return a {@code ResponseEntity} containing the updated {@link Movie}
     */
    @PatchMapping("update/{imdbId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Movie> updateMovie(@PathVariable String imdbId,
            @Valid @RequestBody UpdateMovieRequest updMovie) {

        log.info("Received request to UPDATE THE movie with imdbId {}", imdbId);
        Movie movie = movieService.updateMovie(imdbId, updMovie);
        log.info("The movie with imdbId {} was updated successfully", movie.getImdbId());

        return ResponseEntity.ok(movie);
    }

}
