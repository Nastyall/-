package com.example.restaurantrating.dto;

public record ReviewResponseDTO(
        Long visitorId,
        Long restaurantId,
        Integer rating,
        String reviewText
) {}