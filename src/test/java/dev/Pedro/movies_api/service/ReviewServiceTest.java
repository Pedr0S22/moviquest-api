package dev.Pedro.movies_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import dev.Pedro.movies_api.dto.request.ReviewRequest;
import dev.Pedro.movies_api.exception.MovieNotFoundException;
import dev.Pedro.movies_api.exception.ReviewNotFoundException;
import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.model.Review;
import dev.Pedro.movies_api.repository.MovieRepository;
import dev.Pedro.movies_api.repository.ReviewRepository;
import dev.Pedro.movies_api.security.service.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MovieService movieService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ReviewService reviewService;

    private Authentication authentication;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setup() {
        authentication = mock(Authentication.class);
        userDetails = new UserDetailsImpl(new ObjectId(), "john", "john@mail.com", "pass", Set.of());
    }

    @Test
    void testCreateReview_success() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        ReviewRequest request = new ReviewRequest("tt123", "Great movie!");

        when(movieService.verifyMovieExistence("tt123")).thenReturn(true);

        Review inserted = new Review("Great movie!", "john");
        inserted.setId(new ObjectId());
        when(reviewRepository.insert(any(Review.class))).thenReturn(inserted);

        // Deep-stub the fluent update chain
        when(mongoTemplate.update(eq(Movie.class))
                .matching(any(Criteria.class))
                .apply(any(Update.class))
                .first())
                .thenReturn(null);

        Review result = reviewService.createReview(request);

        assertNotNull(result);
        assertEquals("john", result.getAuthor());
        verify(reviewRepository, times(1)).insert(any(Review.class));
        verify(mongoTemplate, times(1)).update(Movie.class);

        SecurityContextHolder.clearContext();

    }

    @Test
    void testCreateReview_movieNotFound() {
        ReviewRequest request = new ReviewRequest("tt999", "Nice movie!");
        when(movieService.verifyMovieExistence("tt999")).thenReturn(false);

        assertThrows(MovieNotFoundException.class, () -> reviewService.createReview(request));
        verify(reviewRepository, never()).insert(any(Review.class));
    }

    // --- deleteReview tests ---
    @Test
    void testDeleteReview_success() {
        String imdbId = "tt123";
        String reviewId = new ObjectId().toHexString();

        Review review = new Review("Body", "john");
        review.setId(new ObjectId(reviewId));

        Movie movie = new Movie();
        movie.setImdbId(imdbId);
        // use mutable list because service does removeIf()
        movie.setReviewIds(new ArrayList<>(List.of(review)));

        when(movieService.singleMovie(imdbId)).thenReturn(movie);
        when(reviewRepository.existsById(new ObjectId(reviewId))).thenReturn(true);

        reviewService.deleteReview(imdbId, reviewId);

        verify(movieRepository).save(movie);
        verify(reviewRepository).deleteById(new ObjectId(reviewId));
    }

    @Test
    void testDeleteReview_notInMovie() {
        String imdbId = "tt123";
        String reviewId = new ObjectId().toHexString();

        Movie movie = new Movie();
        movie.setImdbId(imdbId);
        movie.setReviewIds(new ArrayList<>()); // no reviews

        when(movieService.singleMovie(imdbId)).thenReturn(movie);
        when(reviewRepository.existsById(new ObjectId(reviewId))).thenReturn(true);

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.deleteReview(imdbId, reviewId));

        verify(movieRepository, never()).save(any());
        verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteReview_notFound() {
        String imdbId = "tt123";
        String reviewId = new ObjectId().toHexString();

        Movie movie = new Movie();
        movie.setImdbId(imdbId);

        when(movieService.singleMovie(imdbId)).thenReturn(movie);
        when(reviewRepository.existsById(new ObjectId(reviewId))).thenReturn(false);

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.deleteReview(imdbId, reviewId));
    }

    // --- updateReview tests ---
    @Test
    void testUpdateReview_success() {
        String imdbId = "tt123";
        String reviewId = new ObjectId().toHexString();
        ReviewRequest request = new ReviewRequest(imdbId, "Updated text");

        Review review = new Review("Old text", "john");
        review.setId(new ObjectId(reviewId));

        Movie movie = new Movie();
        movie.setImdbId(imdbId);
        movie.setReviewIds(List.of(review));

        when(movieService.singleMovie(imdbId)).thenReturn(movie);
        when(reviewRepository.findById(new ObjectId(reviewId))).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review result = reviewService.updateReview(request, reviewId);

        assertEquals("Updated text", result.getBody());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void testUpdateReview_reviewNotInMovie() {
        String imdbId = "tt123";
        String reviewId = new ObjectId().toHexString();
        ReviewRequest request = new ReviewRequest(imdbId, "Body");

        Movie movie = new Movie();
        movie.setImdbId(imdbId);
        movie.setReviewIds(List.of()); // review missing

        when(movieService.singleMovie(imdbId)).thenReturn(movie);

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.updateReview(request, reviewId));
    }

    @Test
    void testUpdateReview_reviewNotFound() {
        String imdbId = "tt123";
        String reviewId = new ObjectId().toHexString();
        ReviewRequest request = new ReviewRequest(imdbId, "Body");

        Review existing = new Review("test", "john");
        existing.setId(new ObjectId()); // ensure non-null id

        Movie movie = new Movie();
        movie.setImdbId(imdbId);
        movie.setReviewIds(List.of(existing));

        when(movieService.singleMovie(imdbId)).thenReturn(movie);

        assertThrows(ReviewNotFoundException.class,
                () -> reviewService.updateReview(request, reviewId));
    }
}
