package dev.Pedro.movies_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import dev.Pedro.movies_api.exception.MovieNotFoundException;
import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> AllMovies() {
        return movieRepository.findAll();
    }

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

    public Boolean verifyMovieExistence(String imdbId) {
        Optional<Movie> movie = movieRepository.findMovieByImdbId(imdbId);

        if (movie.isPresent())
            return true;
        else
            return false;
    }
}