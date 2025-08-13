package dev.Pedro.movies_api.service;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.model.Review;
import dev.Pedro.movies_api.dto.request.ReviewRequest;
import dev.Pedro.movies_api.exception.*;
import dev.Pedro.movies_api.repository.ReviewRepository;
import dev.Pedro.movies_api.security.service.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;

import dev.Pedro.movies_api.repository.MovieRepository;

@Service
@Slf4j
public class ReviewService {

    private final MovieRepository movieRepository;

    private final ReviewRepository reviewRepository;

    private final MovieService movieService;

    private final MongoTemplate mongoTemplate;

    public ReviewService(ReviewRepository reviewRepository, MovieService movieService, MongoTemplate mongoTemplate,
            MovieRepository movieRepository) {
        this.reviewRepository = reviewRepository;
        this.movieService = movieService;
        this.mongoTemplate = mongoTemplate;
        this.movieRepository = movieRepository;
    }

    public Review createReview(ReviewRequest newReview) {

        if (!movieService.verifyMovieExistence(newReview.getImdbId())) {

            String errorMessage = "The movie with imdbId " + newReview.getImdbId()
                    + " does not exist, so the Review was not created";
            log.debug(errorMessage);

            throw new MovieNotFoundException(errorMessage);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();

        String currentUsername = principal.getUsername();

        Review review = reviewRepository.insert(new Review(newReview.getBody(), currentUsername));

        mongoTemplate.update(Movie.class)
                .matching(Criteria.where("imdbId").is(newReview.getImdbId()))
                .apply(new Update().push("reviewIds").value(review.getId()))
                .first();

        return review;
    }

    public void deleteReview(String imdbId, String id) {

        Movie movie = movieService.singleMovie(imdbId);

        if (verifyReviewExistence(id)) {

            // verify if that review exists in that movie and removes it
            boolean removed = movie.getReviewIds().removeIf(review -> review.getId().toHexString().equals(id));

            if (!removed)
                throw new ReviewNotFoundException(
                        "Review with id " + id + " not found from movie with imdbId " + imdbId);

            movieRepository.save(movie);
            reviewRepository.deleteById(new ObjectId(id));
        } else
            throw new ReviewNotFoundException(
                    "Review with id " + id + " not found");
    }

    public Review updateReview(ReviewRequest reviewRequest, String id) {

        // Check movie existence and retrieve it
        Movie movie = movieService.singleMovie(reviewRequest.getImdbId());

        // Check if review belongs to this movie
        boolean reviewInMovie = movie.getReviewIds().stream()
                .anyMatch(r -> r.getId().toHexString().equals(id));
        if (!reviewInMovie)
            throw new ReviewNotFoundException(
                    "Review with id " + id + " not found in movie with imdbId " + reviewRequest.getImdbId());

        // verify if that review exists in that movie and retrieves it
        Review review = reviewRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ReviewNotFoundException("Review with id " + id + " not found"));

        review.setBody(reviewRequest.getBody());

        return reviewRepository.save(review);
    }

    private boolean verifyReviewExistence(String id) {
        return reviewRepository.existsById(new ObjectId(id));
    }
}
