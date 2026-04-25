package com.example.restaurantrating.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CuisineType cuisineType;

    @Column(name = "average_bill", nullable = false, precision = 10, scale = 2)
    private BigDecimal averageBill;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "total_ratings")
    private Integer totalRatings;

    @Column(name = "sum_ratings", precision = 10, scale = 2)
    private BigDecimal sumRatings;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Rating> ratings = new ArrayList<>();

    public Restaurant(Long id, String name, String description, CuisineType cuisineType,
                      BigDecimal averageBill, BigDecimal averageRating) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.cuisineType = cuisineType;
        this.averageBill = averageBill;
        this.averageRating = averageRating;
        this.totalRatings = 0;
        this.sumRatings = BigDecimal.ZERO;
    }

    public void updateAverageRating(BigDecimal newRating) {
        if (this.sumRatings == null) {
            this.sumRatings = BigDecimal.ZERO;
        }
        if (this.totalRatings == null || this.totalRatings == 0) {
            this.sumRatings = newRating;
            this.totalRatings = 1;
            this.averageRating = newRating.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.sumRatings = this.sumRatings.add(newRating);
            this.totalRatings++;
            this.averageRating = this.sumRatings
                    .divide(BigDecimal.valueOf(totalRatings), 2, RoundingMode.HALF_UP);
        }
    }
}