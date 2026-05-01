package com.example.restaurantrating.controller;

import com.example.restaurantrating.dto.ReviewRequestDTO;
import com.example.restaurantrating.model.CuisineType;
import com.example.restaurantrating.model.Restaurant;
import com.example.restaurantrating.model.Review;
import com.example.restaurantrating.model.User;
import com.example.restaurantrating.repository.RestaurantRepository;
import com.example.restaurantrating.repository.ReviewRepository;
import com.example.restaurantrating.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private User testUser;
    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        userRepository.deleteAll();
        restaurantRepository.deleteAll();

        testUser = createUserInDb("Тестовый Пользователь", 30, "Мужской");
        testRestaurant = createRestaurantInDb("Тестовый Ресторан", "ITALIAN", new BigDecimal("2000"));
    }

    @Test
    void createReview_WithValidData_ShouldReturnCreated() throws Exception {
        ReviewRequestDTO request = new ReviewRequestDTO(
                testUser.getId(),
                testRestaurant.getId(),
                5,
                "Отличный ресторан!"
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visitorId").value(testUser.getId()))
                .andExpect(jsonPath("$.restaurantId").value(testRestaurant.getId()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.reviewText").value("Отличный ресторан!"));
    }

    @Test
    void createReview_WhenDuplicate_ShouldReturnBadRequest() throws Exception {
        ReviewRequestDTO request = new ReviewRequestDTO(
                testUser.getId(),
                testRestaurant.getId(),
                4,
                "Первый отзыв"
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReview_WithInvalidRating_ShouldReturnBadRequest() throws Exception {
        ReviewRequestDTO request = new ReviewRequestDTO(
                testUser.getId(),
                testRestaurant.getId(),
                6,
                "Отзыв"
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReview_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        ReviewRequestDTO request = new ReviewRequestDTO(
                99999L,
                testRestaurant.getId(),
                5,
                "Отзыв"
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllReviews_ShouldReturnList() throws Exception {
        createReviewInDb(testUser.getId(), testRestaurant.getId(), 5, "Отлично!");

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk());
    }

    @Test
    void getReviewById_WhenExists_ShouldReturnReview() throws Exception {
        createReviewInDb(testUser.getId(), testRestaurant.getId(), 5, "Отлично!");

        mockMvc.perform(get("/api/reviews/{visitorId}/{restaurantId}",
                        testUser.getId(), testRestaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visitorId").value(testUser.getId()))
                .andExpect(jsonPath("$.restaurantId").value(testRestaurant.getId()))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void updateReview_WhenExists_ShouldUpdate() throws Exception {
        createReviewInDb(testUser.getId(), testRestaurant.getId(), 3, "Старый отзыв");
        ReviewRequestDTO updateRequest = new ReviewRequestDTO(
                testUser.getId(),
                testRestaurant.getId(),
                5,
                "Обновленный отзыв!"
        );

        mockMvc.perform(put("/api/reviews/{visitorId}/{restaurantId}",
                        testUser.getId(), testRestaurant.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.reviewText").value("Обновленный отзыв!"));
    }

    @Test
    void deleteReview_WhenExists_ShouldDelete() throws Exception {
        createReviewInDb(testUser.getId(), testRestaurant.getId(), 5, "Для удаления");

        mockMvc.perform(delete("/api/reviews/{visitorId}/{restaurantId}",
                        testUser.getId(), testRestaurant.getId()))
                .andExpect(status().isNoContent());
    }

    private User createUserInDb(String name, int age, String gender) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setGender(gender);
        return userRepository.save(user);
    }

    private Restaurant createRestaurantInDb(String name, String cuisineType, BigDecimal averageBill) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription("Description");
        restaurant.setCuisineType(CuisineType.valueOf(cuisineType));
        restaurant.setAverageBill(averageBill);
        restaurant.setAverageRating(BigDecimal.ZERO);
        restaurant.setTotalRatings(0);
        restaurant.setSumRatings(BigDecimal.ZERO);
        return restaurantRepository.save(restaurant);
    }

    private void createReviewInDb(Long visitorId, Long restaurantId, Integer rating, String text) {
        Review review = new Review();
        review.setVisitorId(visitorId);
        review.setRestaurantId(restaurantId);
        review.setRating(rating);
        review.setReviewText(text);
        reviewRepository.save(review);
    }
}