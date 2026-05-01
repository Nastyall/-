package com.example.restaurantrating.repository;

import com.example.restaurantrating.model.Review;
import com.example.restaurantrating.model.Review.RatingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, RatingId> {
    List<Review> findByRestaurantId(Long restaurantId);

    List<Review> findByVisitorId(Long visitorId);

    Page<Review> findByRestaurantId(Long restaurantId, Pageable pageable);

    Optional<Review> findByVisitorIdAndRestaurantId(Long visitorId, Long restaurantId);

    List<Review> findByRestaurantIdOrderByRatingAsc(Long restaurantId);

    List<Review> findByRestaurantIdOrderByRatingDesc(Long restaurantId);

    boolean existsByVisitorIdAndRestaurantId(Long visitorId, Long restaurantId);
}