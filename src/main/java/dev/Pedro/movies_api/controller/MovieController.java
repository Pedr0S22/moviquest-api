package dev.Pedro.movies_api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Movie> getMovie(@PathVariable String imdbId, HttpServletRequest request) {
        log.info("Received request to get the movie with imdbId {}", imdbId);
        Movie movie = movieService.singleMovie(imdbId);
        log.info("The movie with imdbId {} was provided", imdbId);

        return ResponseEntity.ok(movie);
    }

}
