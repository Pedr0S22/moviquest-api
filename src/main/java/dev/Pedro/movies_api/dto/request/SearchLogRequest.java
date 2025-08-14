package dev.Pedro.movies_api.dto.request;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

@Data
public class SearchLogRequest {

    @Nullable
    private String level;

    @Nullable
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @PastOrPresent
    private LocalDateTime timestampAfter;

    @Nullable
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @PastOrPresent
    private LocalDateTime timestampBefore;

    @Nullable
    private String thread;

    @Nullable
    private String logger;

    @Nullable
    private Object mdc;

    @Nullable
    private String messageKeywords;

    private boolean sortByTimestamp = true;
}
