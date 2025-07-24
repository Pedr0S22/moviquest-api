package dev.Pedro.movies_api;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.mongodb.MongoException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoTimeoutException;

import dev.Pedro.movies_api.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MongoSocketException.class,
            MongoTimeoutException.class,
            MongoSocketReadTimeoutException.class,
            MongoException.class,
            DataAccessException.class })
    public ResponseEntity<Object> handleMongoDatabaseError(Exception ex, HttpServletRequest request) {

        log.error("Database error: {}", ex.getMessage(), ex);
        ApiResponse error = new ApiResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Database error",
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericError(Exception ex, HttpServletRequest request) {

        log.error("Something went wrong with the server: {}", ex.getMessage(), ex);
        ApiResponse error = new ApiResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Generic",
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
