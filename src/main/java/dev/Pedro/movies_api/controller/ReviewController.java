package dev.Pedro.movies_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.Pedro.movies_api.dto.request.ReviewRequest;
import dev.Pedro.movies_api.dto.response.ReviewResponse;
import dev.Pedro.movies_api.model.Review;
import dev.Pedro.movies_api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for handling movie reviews.
 * <p>
 * Provides endpoints to create, update, and delete reviews.
 * Access to some operations is restricted to review owners or administrators.
 * </p>
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/reviews")
public class ReviewController {

        private final ReviewService reviewService;

        /**
         * Creates a new {@code ReviewController} with the required service.
         *
         * @param reviewService the service responsible for review operations
         */
        public ReviewController(ReviewService reviewService) {
                this.reviewService = reviewService;
        }

        /**
         * Creates a new review for a movie.
         *
         * @param newReview the review request containing review text, rating, and the
         *                  associated movie imdbId
         * @return a {@code ResponseEntity} containing a {@link ReviewResponse} with the
         *         created {@link Review},
         *         returned with HTTP status {@code 201 CREATED}
         */
        @PostMapping
        public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest newReview) {

                log.info("Creating Review for Movie with imdbId {}", newReview.getImdbId());

                Review review = reviewService.createReview(newReview);

                String successMessage = "The review was created successfully and associated to the movie with imdbId "
                                + newReview.getImdbId();
                log.info(successMessage);

                ReviewResponse response = new ReviewResponse(
                                HttpStatus.CREATED.value(),
                                successMessage,
                                review);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Deletes an existing review by its ID and associated movie imdbId.
         * <p>
         * Access is restricted to the review's owner or administrators via the
         * {@code @reviewSecurity.isOwner} security expression.
         * </p>
         *
         * @param imdbId the imdbId identifier of the movie associated with the review
         * @param id     the unique identifier of the review to delete
         * @return a {@code ResponseEntity} containing a {@link ReviewResponse} with a
         *         success message
         */
        @DeleteMapping("/delete/{imdbId}/{id}")
        @PreAuthorize("@reviewSecurity.isOwner(#id) or hasRole('ADMIN')")
        public ResponseEntity<ReviewResponse> deleteReview(@PathVariable String imdbId, @PathVariable String id) {

                log.info("Received request to DELETE review with id {}, from Movie with imdbId {}", id, imdbId);

                reviewService.deleteReview(imdbId, id);

                String successMessage = "The review with id " + id + " from movie with imdbId " + imdbId
                                + " was deleted successfully";
                log.info(successMessage);

                ReviewResponse response = new ReviewResponse(
                                HttpStatus.OK.value(),
                                successMessage);

                return ResponseEntity.ok(response);
        }

        /**
         * Updates an existing review.
         * <p>
         * Only the review owner can perform this action, enforced by the
         * {@code @reviewSecurity.isOwner} security expression.
         * </p>
         *
         * @param id            the unique identifier of the review to update
         * @param reviewRequest the updated review data (rating, text, associated movie
         *                      ID)
         * @return a {@code ResponseEntity} containing a {@link ReviewResponse} with the
         *         updated {@link Review}
         */
        @PatchMapping("/update/{id}")
        @PreAuthorize("@reviewSecurity.isOwner(#id)")
        public ResponseEntity<ReviewResponse> updateReview(@PathVariable String id,
                        @Valid @RequestBody ReviewRequest reviewRequest) {

                log.info("Received request to UPDATE review with id {}, from Movie with imdbId {}", id,
                                reviewRequest.getImdbId());

                Review review = reviewService.updateReview(reviewRequest, id);

                String successMessage = "The review with id " + id + " from movie with imdbId "
                                + reviewRequest.getImdbId()
                                + " was updated successfully";
                log.info(successMessage);

                ReviewResponse response = new ReviewResponse(HttpStatus.OK.value(), successMessage, review);

                return ResponseEntity.ok(response);
        }

}
