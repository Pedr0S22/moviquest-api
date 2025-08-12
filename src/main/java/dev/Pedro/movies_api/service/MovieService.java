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
import dev.Pedro.movies_api.dto.response.ApiResponse;
import dev.Pedro.movies_api.exception.MovieAlreadyExistsException;
import dev.Pedro.movies_api.exception.MovieNotFoundException;
import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.repository.MovieRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MovieService {

    private final MovieRepository movieRepository;
    private final MongoTemplate mongoTemplate;

    public MovieService(MovieRepository movieRepository, MongoTemplate mongoTemplate) {
        this.movieRepository = movieRepository;
        this.mongoTemplate = mongoTemplate;
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

    public Boolean verifyMovieExistence(String imdbId) {
        Optional<Movie> movie = movieRepository.findMovieByImdbId(imdbId);

        if (movie.isPresent())
            return true;
        else
            return false;
    }
}