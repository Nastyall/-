package com.example.restaurantrating.controller;

import com.example.restaurantrating.dto.RestaurantRequestDTO;
import com.example.restaurantrating.model.CuisineType;
import com.example.restaurantrating.model.Restaurant;
import com.example.restaurantrating.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestaurantControllerTest extends BaseControllerTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
    }

    @Test
    void createRestaurant_WithValidData_ShouldReturnCreated() throws Exception {
        RestaurantRequestDTO request = new RestaurantRequestDTO(
                "Итальянский Дворик",
                "Уютный итальянский ресторан",
                "ITALIAN",
                new BigDecimal("2500")
        );

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Итальянский Дворик"))
                .andExpect(jsonPath("$.cuisineType").value("Итальянская"))
                .andExpect(jsonPath("$.averageBill").value(2500));
    }

    @Test
    void createRestaurant_WithInvalidName_ShouldReturnBadRequest() throws Exception {
        RestaurantRequestDTO request = new RestaurantRequestDTO(
                "A", "Description", "ITALIAN", new BigDecimal("1000")
        );

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createRestaurant_WithInvalidCuisine_ShouldReturnBadRequest() throws Exception {
        RestaurantRequestDTO request = new RestaurantRequestDTO(
                "Ресторан", "Description", "INVALID_CUISINE", new BigDecimal("1000")
        );

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRestaurant_WithNegativeAverageBill_ShouldReturnBadRequest() throws Exception {
        RestaurantRequestDTO request = new RestaurantRequestDTO(
                "Ресторан", "Description", "ITALIAN", new BigDecimal("-100")
        );

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.averageBill").exists());
    }

    @Test
    void getAllRestaurants_ShouldReturnList() throws Exception {
        createRestaurantInDb("Ресторан1", CuisineType.ITALIAN, new BigDecimal("2000"));
        createRestaurantInDb("Ресторан2", CuisineType.CHINESE, new BigDecimal("1500"));

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Ресторан1"))
                .andExpect(jsonPath("$[1].name").value("Ресторан2"));
    }

    @Test
    void getRestaurantById_WhenExists_ShouldReturnRestaurant() throws Exception {
        Restaurant saved = createRestaurantInDb("Тестовый Ресторан", CuisineType.JAPANESE, new BigDecimal("3000"));

        mockMvc.perform(get("/api/restaurants/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Тестовый Ресторан"))
                .andExpect(jsonPath("$.cuisineType").value("Японская"));
    }

    @Test
    void getRestaurantById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/restaurants/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRestaurantsByMinRating_ShouldReturnFilteredList() throws Exception {
        Restaurant r1 = createRestaurantInDb("Ресторан1", CuisineType.ITALIAN, new BigDecimal("2000"));
        Restaurant r2 = createRestaurantInDb("Ресторан2", CuisineType.CHINESE, new BigDecimal("1500"));

        r1.setAverageRating(new BigDecimal("4.5"));
        r1.setTotalRatings(2);
        r2.setAverageRating(new BigDecimal("3.0"));
        restaurantRepository.saveAll(List.of(r1, r2));

        mockMvc.perform(get("/api/restaurants/search/by-rating")
                        .param("minRating", "4.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ресторан1"));
    }

    @Test
    void updateRestaurant_WhenExists_ShouldUpdate() throws Exception {
        Restaurant saved = createRestaurantInDb("СтароеНазвание", CuisineType.ITALIAN, new BigDecimal("2000"));
        RestaurantRequestDTO updateRequest = new RestaurantRequestDTO(
                "НовоеНазвание", "Новое описание", "FRENCH", new BigDecimal("3500")
        );

        mockMvc.perform(put("/api/restaurants/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("НовоеНазвание"))
                .andExpect(jsonPath("$.cuisineType").value("Французская"))
                .andExpect(jsonPath("$.averageBill").value(3500));
    }

    @Test
    void deleteRestaurant_WhenExists_ShouldDelete() throws Exception {
        Restaurant saved = createRestaurantInDb("ДляУдаления", CuisineType.ITALIAN, new BigDecimal("2000"));

        mockMvc.perform(delete("/api/restaurants/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(restaurantRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteRestaurant_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/restaurants/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    private Restaurant createRestaurantInDb(String name, CuisineType cuisineType, BigDecimal averageBill) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription("Description");
        restaurant.setCuisineType(cuisineType);
        restaurant.setAverageBill(averageBill);
        restaurant.setAverageRating(BigDecimal.ZERO);
        restaurant.setTotalRatings(0);
        return restaurantRepository.save(restaurant);
    }
}