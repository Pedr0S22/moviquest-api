package dev.Pedro.movies_api.dto.request;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

@Data
public class NewMovieRequest {

    @NotBlank
    private String imdbId;

    @NotBlank
    private String title;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @Nullable
    @URL
    private String trailerLink;

    @Nullable
    @URL
    private String poster;

    @NotEmpty
    private List<@NotBlank String> genres;

    @Nullable
    private List<@NotBlank @URL String> backdrops;

    @Null
    private List<String> reviewIds;
}
