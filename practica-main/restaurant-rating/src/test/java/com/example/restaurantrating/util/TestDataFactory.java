package com.example.restaurantrating.util;

import com.example.restaurantrating.dto.RestaurantRequestDTO;
import com.example.restaurantrating.dto.ReviewRequestDTO;
import com.example.restaurantrating.dto.UserRequestDTO;
import com.example.restaurantrating.model.*;

import java.math.BigDecimal;

public class TestDataFactory {

    public static UserRequestDTO createValidUserRequest() {
        return new UserRequestDTO("Тестовый Пользователь", 30, "Мужской");
    }

    public static UserRequestDTO createInvalidUserRequest() {
        return new UserRequestDTO("A", 16, "");
    }

    public static User createUser(Long id, String name, Integer age, String gender) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setAge(age);
        user.setGender(gender);
        return user;
    }

    public static RestaurantRequestDTO createValidRestaurantRequest() {
        return new RestaurantRequestDTO(
                "Тестовый Ресторан",
                "Описание тестового ресторана",
                "ITALIAN",
                new BigDecimal("2000")
        );
    }

    public static RestaurantRequestDTO createInvalidRestaurantRequest() {
        return new RestaurantRequestDTO(
                "T",
                null,
                "INVALID_CUISINE",
                new BigDecimal("-100")
        );
    }

    public static Restaurant createRestaurant(Long id, String name, String description,
                                              CuisineType cuisineType, BigDecimal averageBill) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setCuisineType(cuisineType);
        restaurant.setAverageBill(averageBill);
        restaurant.setAverageRating(BigDecimal.ZERO);
        restaurant.setTotalRatings(0);
        restaurant.setSumRatings(BigDecimal.ZERO);
        return restaurant;
    }

    public static ReviewRequestDTO createValidReviewRequest(Long visitorId, Long restaurantId) {
        return new ReviewRequestDTO(visitorId, restaurantId, 5, "Отличный ресторан!");
    }

    public static Review createReview(Long visitorId, Long restaurantId, Integer rating, String text) {
        Review review = new Review();
        review.setVisitorId(visitorId);
        review.setRestaurantId(restaurantId);
        review.setRating(rating);
        review.setReviewText(text);
        return review;
    }
}