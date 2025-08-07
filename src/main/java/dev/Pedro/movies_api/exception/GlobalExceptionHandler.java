package dev.Pedro.movies_api.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
                        DataAccessException.class
        })
        public ResponseEntity<Object> handleMongoDatabaseError(Exception ex, HttpServletRequest request) {
                log.error("Database error: {}", ex.getMessage(), ex);
                ApiResponse error = new ApiResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Database error",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        @ExceptionHandler(InvalidReviewCreationRequestException.class)
        public ResponseEntity<Object> handleInvalidReviewCreationRequest(InvalidReviewCreationRequestException ex,
                        HttpServletRequest request) {

                ApiResponse error = new ApiResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad request",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.badRequest().body(error);
        }

        @ExceptionHandler(MovieNotFoundException.class)
        public ResponseEntity<Object> handleMovieNotFound(MovieNotFoundException ex,
                        HttpServletRequest request) {

                ApiResponse error = new ApiResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Not found",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
                log.warn("No resource found for path: {}", request.getRequestURI());

                ApiResponse error = new ApiResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Resource not found",
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<Object> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
                ApiResponse error = new ApiResponse(
                                HttpStatus.NOT_FOUND.value(),
                                "Username Not found",
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<Object> handleEmailInDB(EmailAlreadyExistsException ex, HttpServletRequest request) {
                ApiResponse error = new ApiResponse(
                                HttpStatus.CONFLICT.value(),
                                "Conflict resources with email",
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(UsernameAlreadyExistsException.class)
        public ResponseEntity<Object> handleUsernameInDB(UsernameAlreadyExistsException ex,
                        HttpServletRequest request) {
                ApiResponse error = new ApiResponse(
                                HttpStatus.CONFLICT.value(),
                                "Conflict resources with username",
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(RoleNotFoundException.class)
        public ResponseEntity<Object> handleRoleNotFound(RoleNotFoundException ex, HttpServletRequest request) {
                ApiResponse error = new ApiResponse(
                                HttpStatus.CONFLICT.value(),
                                "Conflict with Role not found",
                                ex.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Object> handleGenericError(Exception ex, HttpServletRequest request) {
                log.error("Unhandled server error: {} - {}", ex.getMessage(), ex.getClass());
                ApiResponse error = new ApiResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal server error",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
}
