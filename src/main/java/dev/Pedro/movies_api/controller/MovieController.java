package dev.Pedro.movies_api.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.Pedro.movies_api.model.CustomError;
import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        log.info("Received request to get all movies.");
        List<Movie> movies = movieService.AllMovies();
        log.info("Returning {} movies.", movies.size());

        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{imdbId}")
    public ResponseEntity<Object> getMovie(@PathVariable String imdbId, HttpServletRequest request) {
        log.info("Received request to get the movie with imdbId {}", imdbId);
        Optional<Movie> movie = movieService.singleMovie(imdbId);

        if (movie.isPresent()) {
            log.info("The movie with imdbId {} was provided", imdbId);
            return ResponseEntity.ok(movie.get());
        } else {

            String errorMessage = "The movie with imdbId " + imdbId + " does not exist";
            log.debug(errorMessage);

            CustomError notFound = new CustomError(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    errorMessage,
                    request.getRequestURI());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound);
        }
    }

}
