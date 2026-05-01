package com.example.restaurantrating.service;

import com.example.restaurantrating.dto.ReviewRequestDTO;
import com.example.restaurantrating.dto.ReviewResponseDTO;
import com.example.restaurantrating.exception.BadRequestException;
import com.example.restaurantrating.exception.ForbiddenException;
import com.example.restaurantrating.model.Review;
import com.example.restaurantrating.model.Review.RatingId;
import com.example.restaurantrating.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final RestaurantService restaurantService;
    private final UserService userService;

    public ReviewResponseDTO createReview(@Valid ReviewRequestDTO request) {
        userService.getUserById(request.visitorId());
        restaurantService.getRestaurantById(request.restaurantId());

        if (reviewRepository.existsByVisitorIdAndRestaurantId(request.visitorId(), request.restaurantId())) {
            throw new BadRequestException("Посетитель уже оставил отзыв для этого ресторана");
        }

        Review review = new Review();
        review.setVisitorId(request.visitorId());
        review.setRestaurantId(request.restaurantId());
        review.setRating(request.rating());
        review.setReviewText(request.reviewText());

        Review saved = reviewRepository.save(review);

        List<Review> restaurantReviews = reviewRepository.findByRestaurantId(request.restaurantId());
        List<Integer> ratings = restaurantReviews.stream()
                .map(Review::getRating)
                .collect(Collectors.toList());
        restaurantService.recalculateAverageRating(request.restaurantId(), ratings);

        return new ReviewResponseDTO(
                saved.getVisitorId(),
                saved.getRestaurantId(),
                saved.getRating(),
                saved.getReviewText()
        );
    }

    public boolean deleteReview(Long visitorId, Long restaurantId) {
        if (reviewRepository.existsByVisitorIdAndRestaurantId(visitorId, restaurantId)) {
            RatingId ratingId = new RatingId(visitorId, restaurantId);
            reviewRepository.deleteById(ratingId);

            List<Review> restaurantReviews = reviewRepository.findByRestaurantId(restaurantId);
            List<Integer> ratings = restaurantReviews.stream()
                    .map(Review::getRating)
                    .collect(Collectors.toList());
            restaurantService.recalculateAverageRating(restaurantId, ratings);
            return true;
        }
        return false;
    }

    public ReviewResponseDTO updateReview(Long visitorId, Long restaurantId, @Valid ReviewRequestDTO request) {
        Review existingReview = reviewRepository.findByVisitorIdAndRestaurantId(visitorId, restaurantId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        if (!existingReview.getVisitorId().equals(visitorId)) {
            throw new ForbiddenException("Нельзя изменить чужой отзыв");
        }

        existingReview.setRating(request.rating());
        existingReview.setReviewText(request.reviewText());

        Review updated = reviewRepository.save(existingReview);

        List<Review> restaurantReviews = reviewRepository.findByRestaurantId(restaurantId);
        List<Integer> ratings = restaurantReviews.stream()
                .map(Review::getRating)
                .collect(Collectors.toList());
        restaurantService.recalculateAverageRating(restaurantId, ratings);

        return new ReviewResponseDTO(
                updated.getVisitorId(),
                updated.getRestaurantId(),
                updated.getRating(),
                updated.getReviewText()
        );
    }

    public List<ReviewResponseDTO> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(r -> new ReviewResponseDTO(
                        r.getVisitorId(),
                        r.getRestaurantId(),
                        r.getRating(),
                        r.getReviewText()
                ))
                .collect(Collectors.toList());
    }

    public ReviewResponseDTO getReviewById(Long visitorId, Long restaurantId) {
        Review review = reviewRepository.findByVisitorIdAndRestaurantId(visitorId, restaurantId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));
        return new ReviewResponseDTO(
                review.getVisitorId(),
                review.getRestaurantId(),
                review.getRating(),
                review.getReviewText()
        );
    }

    public List<ReviewResponseDTO> getReviewsByRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantId(restaurantId).stream()
                .map(r -> new ReviewResponseDTO(
                        r.getVisitorId(),
                        r.getRestaurantId(),
                        r.getRating(),
                        r.getReviewText()
                ))
                .collect(Collectors.toList());
    }

    public Page<ReviewResponseDTO> getReviewsByRestaurantPaged(Long restaurantId, Pageable pageable) {
        return reviewRepository.findByRestaurantId(restaurantId, pageable)
                .map(r -> new ReviewResponseDTO(
                        r.getVisitorId(),
                        r.getRestaurantId(),
                        r.getRating(),
                        r.getReviewText()
                ));
    }

    public List<ReviewResponseDTO> getReviewsByRestaurantSorted(Long restaurantId, String sortOrder) {
        List<Review> reviews;
        if ("asc".equalsIgnoreCase(sortOrder)) {
            reviews = reviewRepository.findByRestaurantIdOrderByRatingAsc(restaurantId);
        } else {
            reviews = reviewRepository.findByRestaurantIdOrderByRatingDesc(restaurantId);
        }
        return reviews.stream()
                .map(r -> new ReviewResponseDTO(
                        r.getVisitorId(),
                        r.getRestaurantId(),
                        r.getRating(),
                        r.getReviewText()
                ))
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getReviewsByUser(Long userId) {
        return reviewRepository.findByVisitorId(userId).stream()
                .map(r -> new ReviewResponseDTO(
                        r.getVisitorId(),
                        r.getRestaurantId(),
                        r.getRating(),
                        r.getReviewText()
                ))
                .collect(Collectors.toList());
    }
}