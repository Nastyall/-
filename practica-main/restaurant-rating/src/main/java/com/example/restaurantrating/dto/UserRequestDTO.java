package com.example.restaurantrating.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank(message = "Имя не может быть пустым")
        @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
        String name,

        @NotNull(message = "Возраст обязателен")
        @Min(value = 18, message = "Возраст должен быть не менее 18 лет")
        Integer age,

        @NotBlank(message = "Пол обязателен")
        @Size(min = 1, max = 20, message = "Пол должен содержать от 1 до 20 символов")
        String gender
) {}