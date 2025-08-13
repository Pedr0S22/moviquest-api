package dev.Pedro.movies_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewReviewRequest {

    @NotBlank
    private String imdbId;

    @NotBlank
    @Size(min = 1, max = 200)
    private String body;
}
