package com.example.restaurantrating.repository;

import com.example.restaurantrating.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT r FROM Restaurant r WHERE r.averageRating >= :minRating")
    List<Restaurant> findRestaurantsWithMinRating(@Param("minRating") BigDecimal minRating);

    List<Restaurant> findByAverageRatingGreaterThanEqual(BigDecimal minRating);
}