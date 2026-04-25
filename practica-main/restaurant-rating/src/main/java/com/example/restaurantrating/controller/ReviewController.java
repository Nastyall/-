package com.example.restaurantrating.controller;

import com.example.restaurantrating.dto.ReviewRequestDTO;
import com.example.restaurantrating.dto.ReviewResponseDTO;
import com.example.restaurantrating.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Отзывы", description = "Управление отзывами и оценками")
public class ReviewController {
    private final RatingService ratingService;

    @PostMapping
    @Operation(summary = "Создать новый отзыв")
    public ResponseEntity<ReviewResponseDTO> createReview(@Valid @RequestBody ReviewRequestDTO request) {
        ReviewResponseDTO review = ratingService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping
    @Operation(summary = "Получить все отзывы")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        return ResponseEntity.ok(ratingService.getAllReviews());
    }

    @GetMapping("/{visitorId}/{restaurantId}")
    @Operation(summary = "Получить отзыв по ID посетителя и ID ресторана")
    public ResponseEntity<ReviewResponseDTO> getReviewById(
            @PathVariable Long visitorId,
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(ratingService.getReviewById(visitorId, restaurantId));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Получить все отзывы ресторана")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(ratingService.getReviewsByRestaurant(restaurantId));
    }

    @GetMapping("/restaurant/{restaurantId}/paged")
    @Operation(summary = "Получить отзывы ресторана постранично")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsByRestaurantPaged(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ratingService.getReviewsByRestaurantPaged(restaurantId, pageable));
    }

    @GetMapping("/restaurant/{restaurantId}/sorted")
    @Operation(summary = "Получить отзывы ресторана с сортировкой по оценке (asc/desc)")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByRestaurantSorted(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "desc") String order) {
        return ResponseEntity.ok(ratingService.getReviewsByRestaurantSorted(restaurantId, order));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Получить все отзывы посетителя")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ratingService.getReviewsByUser(userId));
    }

    @PutMapping("/{visitorId}/{restaurantId}")
    @Operation(summary = "Обновить отзыв")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long visitorId,
            @PathVariable Long restaurantId,
            @Valid @RequestBody ReviewRequestDTO request) {
        return ResponseEntity.ok(ratingService.updateReview(visitorId, restaurantId, request));
    }

    @DeleteMapping("/{visitorId}/{restaurantId}")
    @Operation(summary = "Удалить отзыв")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long visitorId,
            @PathVariable Long restaurantId) {
        boolean deleted = ratingService.deleteReview(visitorId, restaurantId);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}