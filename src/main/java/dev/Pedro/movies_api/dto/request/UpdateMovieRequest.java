package dev.Pedro.movies_api.dto.request;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

@Data
public class UpdateMovieRequest {

    @Nullable
    private String title;

    @Nullable
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent
    private LocalDate releaseDate;

    @Nullable
    @URL
    private String trailerLink;

    @Nullable
    @URL
    private String poster;

    @Nullable
    private List<@NotBlank String> genres;

    @Nullable
    private List<@NotBlank @URL String> backdrops;

}
