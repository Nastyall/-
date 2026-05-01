package com.example.restaurantrating.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(Review.RatingId.class)
public class Review implements Serializable {

    @Id
    private Long visitorId;

    @Id
    private Long restaurantId;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "review_text", length = 1000)
    private String reviewText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitorId", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurantId", referencedColumnName = "id", insertable = false, updatable = false)
    private Restaurant restaurant;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingId implements Serializable {
        private Long visitorId;
        private Long restaurantId;
    }
}