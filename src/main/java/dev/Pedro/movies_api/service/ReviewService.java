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

/**
 * Service class responsible for handling Review operations.
 * <p>
 * This class provides methods to create, update, and delete reviews for movies.
 * It interacts with {@link MovieService}, {@link ReviewRepository}, and
 * {@link MongoTemplate}
 * to ensure proper association of reviews with movies.
 * <p>
 * Security is enforced externally using {@link ReviewSecurity} to verify
 * ownership of reviews.
 */
@Service
@Slf4j
public class ReviewService {

    private final MovieRepository movieRepository;

    private final ReviewRepository reviewRepository;

    private final MovieService movieService;

    private final MongoTemplate mongoTemplate;

    /**
     * Constructs the ReviewService with required repositories and services.
     *
     * @param reviewRepository the repository for persisting reviews
     * @param movieService     the service for managing movies
     * @param mongoTemplate    the MongoTemplate for advanced queries and updates
     * @param movieRepository  the repository for managing movies
     */
    public ReviewService(ReviewRepository reviewRepository, MovieService movieService, MongoTemplate mongoTemplate,
            MovieRepository movieRepository) {
        this.reviewRepository = reviewRepository;
        this.movieService = movieService;
        this.mongoTemplate = mongoTemplate;
        this.movieRepository = movieRepository;
    }

    /**
     * Creates a new review and associates it with the specified movie.
     * <p>
     * Steps performed:
     * <ul>
     * <li>Checks if the movie exists</li>
     * <li>Creates a new review with the currently authenticated user as the
     * author</li>
     * <li>Inserts the review into the database</li>
     * <li>Pushes the review ID into the corresponding movie's {@code reviewIds}
     * list</li>
     * </ul>
     *
     * @param newReview the request containing review body and movie IMDb ID
     * @return the created {@link Review} object
     * @throws MovieNotFoundException if the target movie does not exist
     */
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

    /**
     * Deletes a review and removes its reference from the associated movie.
     * <p>
     * Steps performed:
     * <ul>
     * <li>Verifies the movie exists</li>
     * <li>Checks if the review belongs to the movie</li>
     * <li>Removes the review ID from the movie's {@code reviewIds} list</li>
     * <li>Deletes the review from the database</li>
     * </ul>
     *
     * @param imdbId the IMDb ID of the movie
     * @param id     the ID of the review to delete
     * @throws ReviewNotFoundException if the review does not exist or does not
     *                                 belong to the movie
     */
    public void deleteReview(String imdbId, String id) {

        Movie movie = movieService.singleMovie(imdbId);

        if (reviewRepository.existsById(new ObjectId(id))) {

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

    /**
     * Updates the content of a review.
     * <p>
     * Steps performed:
     * <ul>
     * <li>Verifies the movie exists</li>
     * <li>Checks if the review belongs to the movie</li>
     * <li>Updates the review body</li>
     * <li>Saves the updated review in the database</li>
     * </ul>
     *
     * @param reviewRequest the request containing the new review body and movie
     *                      IMDb ID
     * @param id            the ID of the review to update
     * @return the updated {@link Review} object
     * @throws ReviewNotFoundException if the review does not exist or does not
     *                                 belong to the movie
     */
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

}
