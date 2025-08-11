package dev.Pedro.movies_api.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private int status;
    private String errorType;
    private String message;
    private LocalDateTime localDateTime;
    private String path;

    private Map<String, String> errorsMessage;

    public ApiResponse(int status, String errorType, String message, String path) {
        this.status = status;
        this.errorType = errorType;
        this.message = message;
        this.localDateTime = LocalDateTime.now();
        this.path = path;
    }

    public ApiResponse(int status, String errorType, Map<String, String> errorsMessage, String path) {
        this.status = status;
        this.errorType = errorType;
        this.errorsMessage = errorsMessage;
        this.localDateTime = LocalDateTime.now();
        this.path = path;
    }

}
