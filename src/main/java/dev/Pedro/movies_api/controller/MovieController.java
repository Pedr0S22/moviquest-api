package dev.Pedro.movies_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

@RestController
@Slf4j
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        log.info("Received request to GET all movies.");
        List<Movie> movies = movieService.AllMovies();
        log.info("Returning {} movies.", movies.size());

        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{imdbId}")
    public ResponseEntity<Movie> getMovie(@PathVariable String imdbId) {
        log.info("Received request to GET the movie with imdbId {}", imdbId);
        Movie movie = movieService.singleMovie(imdbId);
        log.info("The movie with imdbId {} was provided", imdbId);

        return ResponseEntity.ok(movie);
    }

    @PostMapping("/search")
    public ResponseEntity<List<Movie>> getMovies(@Valid @RequestBody SearchMoviesRequest search) {

        log.info("Received request to SEARCH movies with filters: {}, {}, {}, {}", search.getTitle(),
                search.getGenres(), search.getReleaseDateAfter(), search.getReleaseDateBefore());

        List<Movie> movies = movieService.searchMovies(search);
        log.info("Returning {} movies.", movies.size());

        return ResponseEntity.ok(movies);
    }

    @DeleteMapping("/delete/{imdbId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteMovie(@PathVariable String imdbId, HttpServletRequest request) {

        log.info("Received request to DELETE the movie with imdbId {}", imdbId);
        ApiResponse deleteResponse = movieService.deleteMovieByImdbId(imdbId, request);
        log.info("The movie with imdbId {} was deleted successfully", imdbId);

        return ResponseEntity.ok(deleteResponse);
    }

    @PostMapping("/newMovie")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Movie> insertMovie(@Valid @RequestBody NewMovieRequest newMovie) {

        log.info("Received request to INSERT a new movie with imdbId {}", newMovie.getImdbId());
        Movie movie = movieService.saveMovie(newMovie);
        log.info("The movie with imdbId {} was inserted successfully", movie.getImdbId());

        return ResponseEntity.status(HttpStatus.CREATED).body(movie);
    }

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
