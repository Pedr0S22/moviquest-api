package dev.Pedro.movies_api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import dev.Pedro.movies_api.model.Review;
import dev.Pedro.movies_api.repository.ReviewRepository;
import dev.Pedro.movies_api.security.service.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
public class ReviewSecurityTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewSecurity reviewSecurity;

    private String id;
    private Authentication authentication;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setSetup() {
        id = new ObjectId().toHexString();

        authentication = mock(Authentication.class);
        userDetails = new UserDetailsImpl(new ObjectId(), "john", "john@mail.com", "password",
                Set.of());
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testIsOwner_success() {

        String id = new ObjectId().toHexString();

        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userDetails = new UserDetailsImpl(new ObjectId(), "john", "john@mail.com", "password",
                Set.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        Review review = new Review();
        review.setId(new ObjectId(id));
        review.setAuthor("john");

        when(reviewRepository.findById(new ObjectId(id)))
                .thenReturn(Optional.of(review));

        boolean assertion = reviewSecurity.isOwner(id);

        assertTrue(assertion);
    }

    @Test
    void testIsOwner_authorizationDenied() {

        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        Review review = new Review();
        review.setId(new ObjectId(id));
        review.setAuthor("notJohn");

        when(reviewRepository.findById(new ObjectId(id)))
                .thenReturn(Optional.of(review));

        assertThrows(AuthorizationDeniedException.class,
                () -> reviewSecurity.isOwner(id));
    }
}
