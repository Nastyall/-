package com.example.restaurantrating.service;

import com.example.restaurantrating.dto.RestaurantRequestDTO;
import com.example.restaurantrating.dto.RestaurantResponseDTO;
import com.example.restaurantrating.model.CuisineType;
import com.example.restaurantrating.model.Restaurant;
import com.example.restaurantrating.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;

    public RestaurantResponseDTO createRestaurant(@Valid RestaurantRequestDTO request) {
        CuisineType cuisineType;
        try {
            cuisineType = CuisineType.valueOf(request.cuisineType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Некорректный тип кухни. Доступные: ITALIAN, CHINESE, JAPANESE, RUSSIAN, FRENCH");
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.name());
        restaurant.setDescription(request.description());
        restaurant.setCuisineType(cuisineType);
        restaurant.setAverageBill(request.averageBill());
        restaurant.setAverageRating(BigDecimal.ZERO);
        restaurant.setTotalRatings(0);
        restaurant.setSumRatings(BigDecimal.ZERO);

        Restaurant saved = restaurantRepository.save(restaurant);
        return toResponseDTO(saved);
    }

    public RestaurantResponseDTO updateRestaurant(Long id, @Valid RestaurantRequestDTO request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ресторан с ID " + id + " не найден"));

        CuisineType cuisineType;
        try {
            cuisineType = CuisineType.valueOf(request.cuisineType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Некорректный тип кухни");
        }

        restaurant.setName(request.name());
        restaurant.setDescription(request.description());
        restaurant.setCuisineType(cuisineType);
        restaurant.setAverageBill(request.averageBill());

        Restaurant updated = restaurantRepository.save(restaurant);
        return toResponseDTO(updated);
    }

    public boolean deleteRestaurant(Long id) {
        if (restaurantRepository.existsById(id)) {
            restaurantRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<RestaurantResponseDTO> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RestaurantResponseDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ресторан с ID " + id + " не найден"));
        return toResponseDTO(restaurant);
    }

    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id).orElse(null);
    }

    public void updateAverageRating(Long restaurantId, BigDecimal newRating) {
        Restaurant restaurant = findById(restaurantId);
        if (restaurant != null) {
            restaurant.updateAverageRating(newRating);
            restaurantRepository.save(restaurant);
        }
    }

    public void recalculateAverageRating(Long restaurantId, List<Integer> ratings) {
        Restaurant restaurant = findById(restaurantId);
        if (restaurant != null) {
            if (ratings.isEmpty()) {
                restaurant.setAverageRating(BigDecimal.ZERO);
                restaurant.setTotalRatings(0);
                restaurant.setSumRatings(BigDecimal.ZERO);
            } else {
                BigDecimal sum = ratings.stream()
                        .map(BigDecimal::valueOf)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                restaurant.setSumRatings(sum);
                restaurant.setTotalRatings(ratings.size());
                restaurant.setAverageRating(sum.divide(BigDecimal.valueOf(ratings.size()), 2, RoundingMode.HALF_UP));
            }
            restaurantRepository.save(restaurant);
        }
    }

    public List<RestaurantResponseDTO> getRestaurantsWithMinRating(BigDecimal minRating) {
        if (minRating == null) {
            minRating = BigDecimal.ZERO;
        }
        return restaurantRepository.findRestaurantsWithMinRating(minRating).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private RestaurantResponseDTO toResponseDTO(Restaurant restaurant) {
        return new RestaurantResponseDTO(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getCuisineType().getDisplayName(),
                restaurant.getAverageBill(),
                restaurant.getAverageRating() != null ? restaurant.getAverageRating() : BigDecimal.ZERO,
                restaurant.getTotalRatings() != null ? restaurant.getTotalRatings() : 0
        );
    }
}