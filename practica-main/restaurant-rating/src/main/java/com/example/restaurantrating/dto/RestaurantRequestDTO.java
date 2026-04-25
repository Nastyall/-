package com.example.restaurantrating.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RestaurantRequestDTO(
        @NotBlank(message = "Название не может быть пустым")
        @Size(min = 2, max = 100, message = "Название должно содержать от 2 до 100 символов")
        String name,

        @Size(max = 500, message = "Описание не должно превышать 500 символов")
        String description,

        @NotBlank(message = "Тип кухни обязателен")
        String cuisineType,

        @NotNull(message = "Средний чек обязателен")
        @Positive(message = "Средний чек должен быть положительным")
        BigDecimal averageBill
) {}