package dev.Pedro.movies_api.service;

import org.bson.types.ObjectId;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import dev.Pedro.movies_api.exception.ReviewNotFoundException;
import dev.Pedro.movies_api.model.Review;
import dev.Pedro.movies_api.repository.ReviewRepository;
import dev.Pedro.movies_api.security.service.UserDetailsImpl;

@Component("reviewSecurity")
public class ReviewSecurity {

    private final ReviewRepository reviewRepository;

    public ReviewSecurity(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public boolean isOwner(String id) {

        if (!ObjectId.isValid(id))
            throw new ReviewNotFoundException("Invalid review id: " + id + ". It must have 24 characters");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) auth.getPrincipal();

        String currentUsername = principal.getUsername();

        Review review = reviewRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new ReviewNotFoundException("Review with id " + id + " not found"));

        if (!review.getAuthor().equals(currentUsername))
            throw new AuthorizationDeniedException("You do not own the review with id " + id);

        return true;
    }
}
