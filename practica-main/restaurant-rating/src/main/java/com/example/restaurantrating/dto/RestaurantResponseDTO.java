package com.example.restaurantrating.dto;

import java.math.BigDecimal;

public record RestaurantResponseDTO(
        Long id,
        String name,
        String description,
        String cuisineType,
        BigDecimal averageBill,
        BigDecimal averageRating,
        Integer totalRatings
) {}