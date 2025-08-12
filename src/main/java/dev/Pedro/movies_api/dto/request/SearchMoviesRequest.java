package dev.Pedro.movies_api.dto.request;

import java.util.Date;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SearchMoviesRequest {

    @Size(min = 2)
    private String title;

    private Set<String> genres;

    @PastOrPresent(message = "Release date after must be in the past or present")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date releaseDateBefore;

    @PastOrPresent(message = "Release date after must be in the past or present")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date releaseDateAfter;

}
