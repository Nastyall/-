package com.example.restaurantrating.service;

import com.example.restaurantrating.dto.ReviewRequestDTO;
import com.example.restaurantrating.dto.ReviewResponseDTO;
import com.example.restaurantrating.model.Rating;
import com.example.restaurantrating.model.Rating.RatingId;
import com.example.restaurantrating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingService {
    private final RatingRepository ratingRepository;
    private final RestaurantService restaurantService;
    private final VisitorService visitorService;

    public ReviewResponseDTO createReview(@Valid ReviewRequestDTO request) {
        visitorService.getUserById(request.visitorId());
        restaurantService.getRestaurantById(request.restaurantId());

        if (ratingRepository.existsByVisitorIdAndRestaurantId(request.visitorId(), request.restaurantId())) {
            throw new RuntimeException("Посетитель уже оставил отзыв для этого ресторана");
        }

        Rating rating = new Rating();
        rating.setVisitorId(request.visitorId());
        rating.setRestaurantId(request.restaurantId());
        rating.setRating(request.rating());
        rating.setReviewText(request.reviewText());

        Rating saved = ratingRepository.save(rating);

        List<Rating> restaurantRatings = ratingRepository.findByRestaurantId(request.restaurantId());
        List<Integer> ratings = restaurantRatings.stream()
                .map(Rating::getRating)
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
        if (ratingRepository.existsByVisitorIdAndRestaurantId(visitorId, restaurantId)) {
            RatingId ratingId = new RatingId(visitorId, restaurantId);
            ratingRepository.deleteById(ratingId);

            List<Rating> restaurantRatings = ratingRepository.findByRestaurantId(restaurantId);
            List<Integer> ratings = restaurantRatings.stream()
                    .map(Rating::getRating)
                    .collect(Collectors.toList());
            restaurantService.recalculateAverageRating(restaurantId, ratings);
            return true;
        }
        return false;
    }

    public ReviewResponseDTO updateReview(Long visitorId, Long restaurantId, @Valid ReviewRequestDTO request) {
        Rating existingRating = ratingRepository.findByVisitorIdAndRestaurantId(visitorId, restaurantId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        existingRating.setRating(request.rating());
        existingRating.setReviewText(request.reviewText());

        Rating updated = ratingRepository.save(existingRating);

        List<Rating> restaurantRatings = ratingRepository.findByRestaurantId(restaurantId);
        List<Integer> ratings = restaurantRatings.stream()
                .map(Rating::getRating)
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
        return ratingRepository.findAll().stream()
                .map(r -> new ReviewResponseDTO(
                        r.getVisitorId(),
                        r.getRestaurantId(),
                        r.getRating(),
                        r.getReviewText()
                ))
                .collect(Collectors.toList());
    }

    public ReviewResponseDTO getReviewById(Long visitorId, Long restaurantId) {
        Rating rating = ratingRepository.findByVisitorIdAndRestaurantId(visitorId, restaurantId)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));
        return new ReviewResponseDTO(
                rating.getVisitorId(),
                rating.getRestaurantId(),
                rating.getRating(),
                rating.getReviewText()
        );
    }

    public List<ReviewResponseDTO> getReviewsByRestaurant(Long restaurantId) {
        return ratingRepository.findByRestaurantId(restaurantId).stream()
                .map(r -> new ReviewResponseDTO(
                        r.getVisitorId(),
                        r.getRestaurantId(),
                        r.getRating(),
                        r.getReviewText()
                ))
                .collect(Collectors.toList());
    }

    public Page<ReviewResponseDTO> getReviewsByRestaurantPaged(Long restaurantId, Pageable pageable) {
        return ratingRepository.findByRestaurantId(restaurantId, pageable)
                .map(r -> new ReviewResponseDTO(
                        r.getVisitorId(),
                        r.getRestaurantId(),
                        r.getRating(),
                        r.getReviewText()
                ));
    }

    public List<ReviewResponseDTO> getReviewsByRestaurantSorted(Long restaurantId, String sortOrder) {
        List<Rating> ratings;
        if ("asc".equalsIgnoreCase(sortOrder)) {
            ratings = ratingRepository.findByRestaurantIdOrderByRatingAsc(restaurantId);
        } else {
            ratings = ratingRepository.findByRestaurantIdOrderByRatingDesc(restaurantId);
        }
        return ratings.stream()
                .map(r -> new ReviewResponseDTO(
                        r.getVisitorId(),
                        r.getRestaurantId(),
                        r.getRating(),
                        r.getReviewText()
                ))
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getReviewsByUser(Long userId) {
        return ratingRepository.findByVisitorId(userId).stream()
                .map(r -> new ReviewResponseDTO(
                        r.getVisitorId(),
                        r.getRestaurantId(),
                        r.getRating(),
                        r.getReviewText()
                ))
                .collect(Collectors.toList());
    }
}