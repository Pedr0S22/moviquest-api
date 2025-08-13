package dev.Pedro.movies_api.dto.response;

import dev.Pedro.movies_api.model.Review;
import lombok.Data;

@Data
public class ReviewResponse {

    private int status;
    private String message;
    private Review review;

    public ReviewResponse(int status, String message, Review review) {
        this.status = status;
        this.message = message;
        this.review = review;
    }
}
