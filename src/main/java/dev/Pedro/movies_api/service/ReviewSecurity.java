package dev.Pedro.movies_api.service;

import org.bson.types.ObjectId;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import dev.Pedro.movies_api.exception.ReviewNotFoundException;
import dev.Pedro.movies_api.model.Review;
import dev.Pedro.movies_api.repository.ReviewRepository;
import dev.Pedro.movies_api.security.service.UserDetailsImpl;

/**
 * Security component to verify ownership of reviews.
 * <p>
 * Used in method-level authorization checks to ensure that the current user
 * is the author of a given review.
 * This component is referenced in SpEL expressions in {@link PreAuthorize}
 * annotations.
 */
@Component("reviewSecurity")
public class ReviewSecurity {

    private final ReviewRepository reviewRepository;

    /**
     * Constructs a ReviewSecurity component with the required repository.
     *
     * @param reviewRepository the repository for accessing reviews
     */
    public ReviewSecurity(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * Checks if the currently authenticated user is the owner of the review.
     * <p>
     * This method:
     * <ul>
     * <li>Validates the review ID format (must be a 24-character ObjectId)</li>
     * <li>Retrieves the review from the database</li>
     * <li>Compares the review's author to the currently authenticated user</li>
     * <li>Throws {@link ReviewNotFoundException} if the review does not exist or ID
     * is invalid</li>
     * <li>Throws {@link AuthorizationDeniedException} if the user is not the
     * owner</li>
     * </ul>
     *
     * @param id the ID of the review to check
     * @return true if the current user is the owner of the review
     * @throws ReviewNotFoundException      if the review does not exist or the ID
     *                                      is invalid
     * @throws AuthorizationDeniedException if the current user is not the author of
     *                                      the review
     */
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
