package dev.Pedro.movies_api.service;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.model.Review;
import dev.Pedro.movies_api.dto.request.NewReviewRequest;
import dev.Pedro.movies_api.exception.*;
import dev.Pedro.movies_api.repository.ReviewRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final MovieService movieService;

    private final MongoTemplate mongoTemplate;

    public ReviewService(ReviewRepository reviewRepository, MovieService movieService, MongoTemplate mongoTemplate) {
        this.reviewRepository = reviewRepository;
        this.movieService = movieService;
        this.mongoTemplate = mongoTemplate;
    }

    public Review createReview(NewReviewRequest newReview) {

        if (!movieService.verifyMovieExistence(newReview.getImdbId())) {

            String errorMessage = "The movie with imdbId " + newReview.getImdbId()
                    + " does not exist, so the Review was not created";
            log.debug(errorMessage);

            throw new MovieNotFoundException(errorMessage);
        }

        Review review = reviewRepository.insert(new Review(newReview.getBody()));

        mongoTemplate.update(Movie.class)
                .matching(Criteria.where("imdbId").is(newReview.getImdbId()))
                .apply(new Update().push("reviewIds").value(review.getId()))
                .first();

        return review;
    }
}
