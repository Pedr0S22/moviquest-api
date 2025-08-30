package dev.Pedro.movies_api.dto.response;

import java.time.LocalDateTime;

import dev.Pedro.movies_api.model.Review;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewResponse {

    private int status;
    private String message;
    private Review review;

    private LocalDateTime localDateTime;

    public ReviewResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.localDateTime = LocalDateTime.now();
    }

    public ReviewResponse(int status, String message, Review review) {
        this.status = status;
        this.message = message;
        this.localDateTime = LocalDateTime.now();
        this.review = review;
    }
}
