package dev.Pedro.movies_api.dto.request;

import java.util.Set;

import org.springframework.data.mongodb.core.mapping.DocumentReference;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    @NotBlank
    @Size(min = 5, max = 20)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 40)
    private String password;

    @DocumentReference
    private Set<String> roles;
}
