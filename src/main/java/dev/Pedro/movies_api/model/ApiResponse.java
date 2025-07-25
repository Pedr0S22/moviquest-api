package dev.Pedro.movies_api.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private int status;
    private String errorType;
    private String message;
    private LocalDateTime localDateTime;
    private String path;
    private Object review;

    public ApiResponse(int status, String errorType, String message, String path) {
        this.status = status;
        this.errorType = errorType;
        this.message = message;
        this.localDateTime = LocalDateTime.now();
        this.path = path;
    }

    public ApiResponse(int status, String message, Object review) {
        this.status = status;
        this.message = message;
        this.review = review;
    }
}
