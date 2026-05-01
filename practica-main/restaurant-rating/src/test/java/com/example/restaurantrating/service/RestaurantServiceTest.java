package com.example.restaurantrating.service;

import com.example.restaurantrating.dto.RestaurantRequestDTO;
import com.example.restaurantrating.dto.RestaurantResponseDTO;
import com.example.restaurantrating.model.CuisineType;
import com.example.restaurantrating.model.Restaurant;
import com.example.restaurantrating.repository.RestaurantRepository;
import com.example.restaurantrating.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant testRestaurant;
    private RestaurantRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        testRestaurant = TestDataFactory.createRestaurant(
                1L,
                "Белые ночи",
                "Уютный ресторан",
                CuisineType.ITALIAN,
                new BigDecimal("2500")
        );
        validRequest = TestDataFactory.createValidRestaurantRequest();
    }

    @Test
    void createRestaurant_WithValidCuisine_ShouldReturnRestaurant() {
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        RestaurantResponseDTO result = restaurantService.createRestaurant(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Белые ночи");
        assertThat(result.averageBill()).isEqualByComparingTo(new BigDecimal("2500"));
        assertThat(result.cuisineType()).isEqualTo("Итальянская");

        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    void createRestaurant_WithInvalidCuisine_ShouldThrowException() {
        RestaurantRequestDTO invalidRequest = new RestaurantRequestDTO(
                "Test", "Desc", "INVALID_CUISINE", new BigDecimal("1000")
        );

        assertThatThrownBy(() -> restaurantService.createRestaurant(invalidRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Некорректный тип кухни");

        verify(restaurantRepository, never()).save(any(Restaurant.class));
    }

    @Test
    void getRestaurantById_WhenExists_ShouldReturnRestaurant() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        RestaurantResponseDTO result = restaurantService.getRestaurantById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Белые ночи");

        verify(restaurantRepository, times(1)).findById(1L);
    }

    @Test
    void getRestaurantById_WhenNotExists_ShouldThrowException() {
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.getRestaurantById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getAllRestaurants_ShouldReturnList() {
        List<Restaurant> restaurants = List.of(
                testRestaurant,
                TestDataFactory.createRestaurant(2L, "Старый город", "Китайская", CuisineType.CHINESE, new BigDecimal("1500"))
        );
        when(restaurantRepository.findAll()).thenReturn(restaurants);

        List<RestaurantResponseDTO> result = restaurantService.getAllRestaurants();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RestaurantResponseDTO::name)
                .containsExactlyInAnyOrder("Белые ночи", "Старый город");
    }

    @Test
    void updateAverageRating_WithValidRating_ShouldUpdate() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        restaurantService.updateAverageRating(1L, new BigDecimal("4.5"));

        assertThat(testRestaurant.getAverageRating()).isEqualByComparingTo(new BigDecimal("4.50"));
        verify(restaurantRepository, times(1)).save(testRestaurant);
    }

    @Test
    void updateAverageRating_WhenRestaurantNotFound_ShouldDoNothing() {
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        restaurantService.updateAverageRating(999L, new BigDecimal("4.5"));

        verify(restaurantRepository, never()).save(any());
    }

    @Test
    void recalculateAverageRating_WithReviews_ShouldCalculateCorrectly() {
        List<Integer> ratings = List.of(5, 4, 5, 3);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        restaurantService.recalculateAverageRating(1L, ratings);

        assertThat(testRestaurant.getAverageRating())
                .isEqualByComparingTo(new BigDecimal("4.25"));
        assertThat(testRestaurant.getTotalRatings()).isEqualTo(4);
        assertThat(testRestaurant.getSumRatings()).isEqualByComparingTo(new BigDecimal("17"));
    }

    @Test
    void recalculateAverageRating_WithNoReviews_ShouldResetToZero() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        restaurantService.recalculateAverageRating(1L, new ArrayList<>());

        assertThat(testRestaurant.getAverageRating()).isZero();
        assertThat(testRestaurant.getTotalRatings()).isZero();
        assertThat(testRestaurant.getSumRatings()).isZero();
    }

    @Test
    void getRestaurantsWithMinRating_ShouldReturnFilteredList() {
        List<Restaurant> restaurants = List.of(testRestaurant);
        when(restaurantRepository.findRestaurantsWithMinRating(new BigDecimal("4.0")))
                .thenReturn(restaurants);

        List<RestaurantResponseDTO> result = restaurantService.getRestaurantsWithMinRating(new BigDecimal("4.0"));

        assertThat(result).hasSize(1);
        verify(restaurantRepository, times(1)).findRestaurantsWithMinRating(new BigDecimal("4.0"));
    }

    @Test
    void getRestaurantsWithMinRating_WithNull_ShouldUseZero() {
        List<Restaurant> restaurants = List.of(testRestaurant);
        when(restaurantRepository.findRestaurantsWithMinRating(BigDecimal.ZERO))
                .thenReturn(restaurants);

        List<RestaurantResponseDTO> result = restaurantService.getRestaurantsWithMinRating(null);

        assertThat(result).hasSize(1);
        verify(restaurantRepository, times(1)).findRestaurantsWithMinRating(BigDecimal.ZERO);
    }

    @Test
    void updateRestaurant_WhenExists_ShouldUpdate() {
        RestaurantRequestDTO updateRequest = new RestaurantRequestDTO(
                "Обновленное название", "Новое описание", "FRENCH", new BigDecimal("3000")
        );
        Restaurant updatedRestaurant = TestDataFactory.createRestaurant(
                1L, "Обновленное название", "Новое описание", CuisineType.FRENCH, new BigDecimal("3000")
        );

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(updatedRestaurant);

        RestaurantResponseDTO result = restaurantService.updateRestaurant(1L, updateRequest);

        assertThat(result.name()).isEqualTo("Обновленное название");
        assertThat(result.cuisineType()).isEqualTo("Французская");
    }

    @Test
    void updateRestaurant_WhenNotExists_ShouldThrowException() {
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.updateRestaurant(999L, validRequest))
                .isInstanceOf(RuntimeException.class);
    }
}