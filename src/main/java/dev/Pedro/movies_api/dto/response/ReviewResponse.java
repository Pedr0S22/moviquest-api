package dev.Pedro.movies_api.dto.response;

import dev.Pedro.movies_api.model.Review;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewResponse {

    private int status;
    private String message;
    private Review review;

    public ReviewResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
