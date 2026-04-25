package com.example.restaurantrating.dto;

public record UserResponseDTO(
        Long id,
        String name,
        Integer age,
        String gender
) {}