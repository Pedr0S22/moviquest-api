package dev.Pedro.movies_api.exception;

public class InvalidReviewCreationRequestException extends RuntimeException {
    public InvalidReviewCreationRequestException(String message) {
        super(message);
    }
}
