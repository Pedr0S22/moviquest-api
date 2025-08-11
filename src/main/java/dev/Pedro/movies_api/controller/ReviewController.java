package dev.Pedro.movies_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.Pedro.movies_api.dto.request.NewReviewRequest;
import dev.Pedro.movies_api.dto.response.ApiResponse;
import dev.Pedro.movies_api.model.Review;
import dev.Pedro.movies_api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Object> createReview(@Valid @RequestBody NewReviewRequest newReview) {

        log.info("Creating Review for Movie with imdbId {}", newReview.getImdbId());

        Review review = reviewService.createReview(newReview);

        String successMessage = "The review was created successfully and associated to the movie with imdbId "
                + newReview.getImdbId();
        log.info(successMessage);

        ApiResponse response = new ApiResponse(
                HttpStatus.CREATED.value(),
                successMessage,
                review);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update review Idea: @PreAuthorize("#review.authorId ==
    // authentication.principal.id or hasRole('ADMIN')")

}
