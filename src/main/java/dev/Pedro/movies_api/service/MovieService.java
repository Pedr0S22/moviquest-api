package dev.Pedro.movies_api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.repository.MovieRepository;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public List<Movie> AllMovies() {
        return movieRepository.findAll();
    }

    public Optional<Movie> singleMovie(String imdbId) {
        return movieRepository.findMovieByImdbId(imdbId);
    }

    public Boolean verifyMovieExistence(String imdbId) {
        Optional<Movie> movie = singleMovie(imdbId);

        if (movie.isPresent())
            return true;
        else
            return false;
    }
}
