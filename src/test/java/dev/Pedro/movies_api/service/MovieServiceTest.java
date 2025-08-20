package dev.Pedro.movies_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;

import dev.Pedro.movies_api.dto.request.NewMovieRequest;
import dev.Pedro.movies_api.dto.request.SearchMoviesRequest;
import dev.Pedro.movies_api.dto.request.UpdateMovieRequest;
import dev.Pedro.movies_api.dto.response.ApiResponse;
import dev.Pedro.movies_api.exception.MovieAlreadyExistsException;
import dev.Pedro.movies_api.exception.MovieNotFoundException;
import dev.Pedro.movies_api.model.Movie;
import dev.Pedro.movies_api.repository.MovieRepository;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MovieService movieService;

    private String imdbId;
    private Movie movie;

    @BeforeEach
    void setup() {
        imdbId = "tt1234567";
        movie = new Movie();
    }

    @Test
    void testSingleMovie_found() {
        movie.setImdbId(imdbId);

        when(movieRepository.findMovieByImdbId(imdbId)).thenReturn(Optional.of(movie));

        Movie result = movieService.singleMovie(imdbId);

        assertEquals(imdbId, result.getImdbId());
    }

    @Test
    void testSingleMovie_notFound() {

        when(movieRepository.findMovieByImdbId(imdbId)).thenReturn(Optional.empty());

        assertThrows(MovieNotFoundException.class, () -> movieService.singleMovie(imdbId));
    }

    @Test
    void testSaveMovie_success() {
        NewMovieRequest request = new NewMovieRequest();
        request.setImdbId("tt1234567");
        request.setTitle("Test Movie");
        request.setReleaseDate(LocalDate.of(2000, 1, 1));
        request.setGenres(List.of("foo", "bar"));

        when(movieRepository.existsByImdbId("tt1234567")).thenReturn(false);
        when(movieRepository.save(any(Movie.class))).thenAnswer(i -> i.getArgument(0));

        Movie saved = movieService.saveMovie(request);

        assertEquals("tt1234567", saved.getImdbId());
        assertEquals("Test Movie", saved.getTitle());
        assertEquals(LocalDate.of(2000, 1, 1).toString(), saved.getReleaseDate());
        assertEquals(List.of("foo", "bar"), saved.getGenres());
    }

    @Test
    void testSaveMovie_alreadyExists() {
        NewMovieRequest request = new NewMovieRequest();
        request.setImdbId("tt1234567");

        when(movieRepository.existsByImdbId("tt1234567")).thenReturn(true);

        assertThrows(MovieAlreadyExistsException.class, () -> movieService.saveMovie(request));
    }

    @Test
    void testDeleteMovieByImdbId_success() {
        String imdbId = "tt1234567";
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/movies/delete");

        when(movieRepository.existsByImdbId(imdbId)).thenReturn(true);

        ApiResponse response = movieService.deleteMovieByImdbId(imdbId, req);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("/movies/delete", response.getPath());
        verify(movieRepository, times(1)).deleteByImdbId(imdbId);
    }

    @Test
    void testDeleteMovieByImdbId_notFound() {
        String imdbId = "tt1234567";
        HttpServletRequest req = mock(HttpServletRequest.class);

        when(movieRepository.existsByImdbId(imdbId)).thenReturn(false);

        assertThrows(MovieNotFoundException.class, () -> movieService.deleteMovieByImdbId(imdbId, req));
    }

    @Test
    void testVerifyMovieExistence_true() {
        when(movieRepository.existsByImdbId("tt1234567")).thenReturn(true);

        assertTrue(movieService.verifyMovieExistence("tt1234567"));
    }

    @Test
    void testVerifyMovieExistence_false() {
        when(movieRepository.existsByImdbId("tt1234567")).thenReturn(false);

        assertFalse(movieService.verifyMovieExistence("tt1234567"));
    }

    @Test
    void testSearchMovies_withMultipleFilters() {
        SearchMoviesRequest request = new SearchMoviesRequest();
        request.setTitle("Matrix");
        request.setGenres(Set.of("Action", "Sci-Fi"));
        request.setReleaseDateAfter(new Date(1000));
        request.setReleaseDateBefore(new Date(2000));

        List<Movie> expectedMovies = List.of(new Movie());

        when(mongoTemplate.find(any(Query.class), eq(Movie.class)))
                .thenReturn(expectedMovies);

        List<Movie> result = movieService.searchMovies(request);

        assertEquals(expectedMovies, result);
        assertEquals(1, result.size());

        // Capture and inspect the actual query to ensure multiple criteria applied
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate, times(1)).find(queryCaptor.capture(), eq(Movie.class));

        Query capturedQuery = queryCaptor.getValue();
        String queryJson = capturedQuery.getQueryObject().toJson();

        assertTrue(queryJson.contains("title"));
        assertTrue(queryJson.contains("genres"));
        assertTrue(queryJson.contains("releaseDate"));
    }

    @Test
    void testSearchMovies_noFilters() {
        SearchMoviesRequest request = new SearchMoviesRequest();

        when(movieRepository.findAll()).thenReturn(List.of(new Movie()));
        when(mongoTemplate.find(any(Query.class), eq(Movie.class)))
                .thenReturn(List.of(new Movie()));

        List<Movie> result = movieService.searchMovies(request);

        assertEquals(1, result.size());
    }

    @Test
    void testUpdateMovie_updateMultipleFields() {
        String imdbId = "tt1234567";

        movie.setImdbId(imdbId);

        UpdateMovieRequest request = new UpdateMovieRequest();
        request.setPoster("newPoster.jpg");
        request.setTrailerLink("newTrailer.mp4");

        when(movieRepository.findMovieByImdbId(imdbId)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(i -> i.getArgument(0));

        Movie updated = movieService.updateMovie(imdbId, request);

        assertEquals("newPoster.jpg", updated.getPoster());
        assertEquals("newTrailer.mp4", updated.getTrailerLink());
    }

    @Test
    void testUpdateMovie_notFound() {
        String imdbId = "tt1234567";
        UpdateMovieRequest request = new UpdateMovieRequest();

        when(movieRepository.findMovieByImdbId(imdbId)).thenReturn(Optional.empty());

        assertThrows(MovieNotFoundException.class,
                () -> movieService.updateMovie(imdbId, request));
    }

}
