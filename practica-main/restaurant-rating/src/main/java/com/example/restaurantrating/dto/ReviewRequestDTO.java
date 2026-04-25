package com.example.restaurantrating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequestDTO(
        @NotNull(message = "ID посетителя обязателен")
        @Min(value = 1, message = "ID посетителя должен быть положительным")
        Long visitorId,

        @NotNull(message = "ID ресторана обязателен")
        @Min(value = 1, message = "ID ресторана должен быть положительным")
        Long restaurantId,

        @NotNull(message = "Оценка обязательна")
        @Min(value = 1, message = "Оценка должна быть от 1 до 5")
        @Max(value = 5, message = "Оценка должна быть от 1 до 5")
        Integer rating,

        @Size(max = 1000, message = "Отзыв не должен превышать 1000 символов")
        String reviewText
) {}