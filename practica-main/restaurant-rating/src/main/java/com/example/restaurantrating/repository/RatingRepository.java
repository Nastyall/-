package com.example.restaurantrating.repository;

import com.example.restaurantrating.model.Rating;
import com.example.restaurantrating.model.Rating.RatingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, RatingId> {
    List<Rating> findByRestaurantId(Long restaurantId);

    List<Rating> findByVisitorId(Long visitorId);

    Page<Rating> findByRestaurantId(Long restaurantId, Pageable pageable);

    Optional<Rating> findByVisitorIdAndRestaurantId(Long visitorId, Long restaurantId);

    @Query("SELECT r FROM Rating r WHERE r.restaurantId = :restaurantId ORDER BY r.rating ASC")
    List<Rating> findByRestaurantIdOrderByRatingAsc(@Param("restaurantId") Long restaurantId);

    @Query("SELECT r FROM Rating r WHERE r.restaurantId = :restaurantId ORDER BY r.rating DESC")
    List<Rating> findByRestaurantIdOrderByRatingDesc(@Param("restaurantId") Long restaurantId);

    boolean existsByVisitorIdAndRestaurantId(Long visitorId, Long restaurantId);
}