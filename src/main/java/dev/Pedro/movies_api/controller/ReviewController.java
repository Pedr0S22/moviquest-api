package dev.Pedro.movies_api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.Pedro.movies_api.model.ApiResponse;
import dev.Pedro.movies_api.model.Review;
import dev.Pedro.movies_api.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Object> createReview(@RequestBody Map<String, String> payload, HttpServletRequest request) {

        String reviewBody = payload.get("reviewBody");
        String imdbId = payload.get("imdbId");

        log.info("Creating Review for Movie with imdbId {}", imdbId);

        Review review = reviewService.createReview(reviewBody, imdbId);

        String successMessage = "The review was created successfully and associated to the movie with imdbId " + imdbId;
        log.info(successMessage);

        ApiResponse response = new ApiResponse(
                HttpStatus.CREATED.value(),
                successMessage,
                review);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
