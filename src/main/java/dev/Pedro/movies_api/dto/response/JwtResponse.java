package dev.Pedro.movies_api.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtResponse {

    private int status;
    private LocalDateTime localDateTime;

    private ObjectId id;
    private String accessToken;
    private String type;
    private String username;
    private String email;
    private Set<String> roles;

    private String message;

    public JwtResponse(int status, String accessToken, String username,
            String email, Set<String> roles) {

        this.status = status;
        this.localDateTime = LocalDateTime.now();
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.type = "Bearer";
        this.accessToken = accessToken;
    }

    public JwtResponse(int status, String username, String email, Set<String> roles, String message) {

        this.status = status;
        this.localDateTime = LocalDateTime.now();
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.message = message;
    }
}
