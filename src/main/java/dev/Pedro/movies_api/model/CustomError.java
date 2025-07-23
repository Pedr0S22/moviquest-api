package dev.Pedro.movies_api.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomError {

    private int status;
    private String errorType;
    private String errorMessage;
    private LocalDateTime timestamp;
    private String path;

    public CustomError(int status, String errorType, String errorMessage, String path) {
        this.status = status;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }
}
