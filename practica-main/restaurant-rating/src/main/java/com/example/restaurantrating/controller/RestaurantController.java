package com.example.restaurantrating.controller;

import com.example.restaurantrating.dto.RestaurantRequestDTO;
import com.example.restaurantrating.dto.RestaurantResponseDTO;
import com.example.restaurantrating.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Рестораны", description = "Управление ресторанами")
@SuppressWarnings({"unused", "SpringJavaInjectionPointsAutowiringInspection"})
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping
    @Operation(summary = "Создать новый ресторан")
    @SuppressWarnings("unused")
    public ResponseEntity<RestaurantResponseDTO> createRestaurant(@Valid @RequestBody RestaurantRequestDTO request) {
        RestaurantResponseDTO restaurant = restaurantService.createRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
    }

    @GetMapping
    @Operation(summary = "Получить все рестораны")
    @SuppressWarnings("unused")
    public ResponseEntity<List<RestaurantResponseDTO>> getAllRestaurants() {
        return ResponseEntity.ok(restaurantService.getAllRestaurants());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить ресторан по ID")
    @SuppressWarnings("unused")
    public ResponseEntity<RestaurantResponseDTO> getRestaurantById(@PathVariable @SuppressWarnings("unused") Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping("/search/by-rating")
    @Operation(summary = "Найти рестораны с оценкой не ниже заданной")
    @SuppressWarnings("unused")
    public ResponseEntity<List<RestaurantResponseDTO>> getRestaurantsByMinRating(
            @RequestParam @SuppressWarnings("unused") BigDecimal minRating) {
        return ResponseEntity.ok(restaurantService.getRestaurantsWithMinRating(minRating));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные ресторана")
    @SuppressWarnings("unused")
    public ResponseEntity<RestaurantResponseDTO> updateRestaurant(
            @PathVariable @SuppressWarnings("unused") Long id,
            @Valid @RequestBody @SuppressWarnings("unused") RestaurantRequestDTO request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить ресторан")
    @SuppressWarnings("unused")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable @SuppressWarnings("unused") Long id) {
        boolean deleted = restaurantService.deleteRestaurant(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}