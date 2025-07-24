package dev.Pedro.movies_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.model.Review;
import dev.Pedro.movies_api.exception.*;
import dev.Pedro.movies_api.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReviewService {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    MovieService movieService;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Review createReview(String reviewBody, String imdbId) {

        if (reviewBody == null || imdbId == null || reviewBody.isBlank() || imdbId.isBlank()) {

            String errorMessage = "Missing ReviewBody and/or imdbId in request";
            log.debug(errorMessage);

            throw new InvalidReviewCreationRequestException(errorMessage);

        } else if (!movieService.verifyMovieExistence(imdbId)) {

            String errorMessage = "The movie with imdbId " + imdbId + " does not exist, so the Review was not created";
            log.debug(errorMessage);

            throw new MovieNotFoundException(errorMessage);
        }

        Review review = reviewRepository.insert(new Review(reviewBody));

        mongoTemplate.update(Movie.class)
                .matching(Criteria.where("imdbId").is(imdbId))
                .apply(new Update().push("reviewIds").value(review.getId()))
                .first();

        return review;
    }
}
