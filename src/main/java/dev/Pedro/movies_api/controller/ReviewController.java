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
import dev.Pedro.movies_api.service.MovieService;
import dev.Pedro.movies_api.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MovieService movieService;

    @PostMapping
    public ResponseEntity<Object> createReview(@RequestBody Map<String, String> payload, HttpServletRequest request) {

        String reviewBody = payload.get("reviewBody");
        String imdbId = payload.get("imdbId");

        log.info("Creating Review for Movie with imdbId {}", imdbId);

        if (reviewBody == null || imdbId == null || reviewBody.isBlank() || imdbId.isBlank()) {

            String errorMessage = "Missing ReviewBody and/or imdbId in request";
            log.debug(errorMessage);

            ApiResponse error = new ApiResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad request",
                    errorMessage,
                    request.getRequestURI());

            return ResponseEntity.badRequest().body(error);

        } else if (!movieService.verifyMovieExistence(imdbId)) {

            String errorMessage = "The movie with imdbId " + imdbId + " does not exist, so the Review was not created";
            log.debug(errorMessage);

            ApiResponse badRequest = new ApiResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    errorMessage,
                    request.getRequestURI());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(badRequest);
        }

        String successMessage = "The review was created successfully and associated to the movie with imdbId" + imdbId;
        log.info(successMessage);

        Object review = reviewService.createReview(reviewBody, imdbId);
        ApiResponse response = new ApiResponse(
                HttpStatus.CREATED.value(),
                successMessage,
                review);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
