package com.example.restaurantrating.repository;

import com.example.restaurantrating.model.CuisineType;
import com.example.restaurantrating.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
    }

    @Test
    void saveAndFindRestaurant_ShouldWork() {
        Restaurant restaurant = createRestaurant("Тестовый", CuisineType.ITALIAN, new BigDecimal("2000"));

        Restaurant saved = restaurantRepository.save(restaurant);
        Restaurant found = restaurantRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Тестовый");
        assertThat(found.getCuisineType()).isEqualTo(CuisineType.ITALIAN);
    }

    @Test
    void findRestaurantsWithMinRating_ShouldReturnFiltered() {
        Restaurant r1 = createRestaurant("Ресторан1", CuisineType.ITALIAN, new BigDecimal("2000"));
        Restaurant r2 = createRestaurant("Ресторан2", CuisineType.CHINESE, new BigDecimal("1500"));

        r1.setAverageRating(new BigDecimal("4.5"));
        r2.setAverageRating(new BigDecimal("3.0"));
        restaurantRepository.saveAll(List.of(r1, r2));

        List<Restaurant> result = restaurantRepository.findRestaurantsWithMinRating(new BigDecimal("4.0"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Ресторан1");
    }

    @Test
    void findByAverageRatingGreaterThanEqual_ShouldReturnFiltered() {
        Restaurant r1 = createRestaurant("Ресторан1", CuisineType.ITALIAN, new BigDecimal("2000"));
        Restaurant r2 = createRestaurant("Ресторан2", CuisineType.CHINESE, new BigDecimal("1500"));

        r1.setAverageRating(new BigDecimal("4.5"));
        r2.setAverageRating(new BigDecimal("3.0"));
        restaurantRepository.saveAll(List.of(r1, r2));

        List<Restaurant> result = restaurantRepository.findByAverageRatingGreaterThanEqual(new BigDecimal("4.0"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Ресторан1");
    }

    @Test
    void findAll_ShouldReturnAllRestaurants() {
        restaurantRepository.save(createRestaurant("Ресторан1", CuisineType.ITALIAN, new BigDecimal("2000")));
        restaurantRepository.save(createRestaurant("Ресторан2", CuisineType.CHINESE, new BigDecimal("1500")));

        List<Restaurant> result = restaurantRepository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteRestaurant_ShouldRemoveRestaurant() {
        Restaurant saved = restaurantRepository.save(createRestaurant("ДляУдаления", CuisineType.ITALIAN, new BigDecimal("2000")));

        restaurantRepository.deleteById(saved.getId());

        assertThat(restaurantRepository.findById(saved.getId())).isEmpty();
    }

    private Restaurant createRestaurant(String name, CuisineType cuisineType, BigDecimal averageBill) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription("Description");
        restaurant.setCuisineType(cuisineType);
        restaurant.setAverageBill(averageBill);
        restaurant.setAverageRating(BigDecimal.ZERO);
        restaurant.setTotalRatings(0);
        restaurant.setSumRatings(BigDecimal.ZERO);
        return restaurant;
    }
}