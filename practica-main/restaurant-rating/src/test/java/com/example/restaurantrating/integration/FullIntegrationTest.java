package com.example.restaurantrating.integration;

import com.example.restaurantrating.dto.RestaurantRequestDTO;
import com.example.restaurantrating.dto.RestaurantResponseDTO;
import com.example.restaurantrating.dto.ReviewRequestDTO;
import com.example.restaurantrating.dto.ReviewResponseDTO;
import com.example.restaurantrating.dto.UserRequestDTO;
import com.example.restaurantrating.dto.UserResponseDTO;
import com.example.restaurantrating.repository.RestaurantRepository;
import com.example.restaurantrating.repository.ReviewRepository;
import com.example.restaurantrating.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FullIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        restaurantRepository.deleteAll();
        userRepository.deleteAll();
    }

    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void fullUserRestaurantReviewFlow_ShouldWorkCorrectly() {
        UserRequestDTO userRequest = new UserRequestDTO("Иван Петров", 30, "Мужской");
        HttpEntity<UserRequestDTO> userEntity = new HttpEntity<>(userRequest, createJsonHeaders());

        ResponseEntity<UserResponseDTO> createUserResponse = restTemplate.exchange(
                "/api/users",
                HttpMethod.POST,
                userEntity,
                UserResponseDTO.class
        );

        assertThat(createUserResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createUserResponse.getBody()).isNotNull();
        Long userId = createUserResponse.getBody().id();

        RestaurantRequestDTO restaurantRequest = new RestaurantRequestDTO(
                "Итальянский Дворик",
                "Уютный итальянский ресторан",
                "ITALIAN",
                new BigDecimal("2500")
        );
        HttpEntity<RestaurantRequestDTO> restaurantEntity = new HttpEntity<>(restaurantRequest, createJsonHeaders());

        ResponseEntity<RestaurantResponseDTO> createRestaurantResponse = restTemplate.exchange(
                "/api/restaurants",
                HttpMethod.POST,
                restaurantEntity,
                RestaurantResponseDTO.class
        );

        assertThat(createRestaurantResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createRestaurantResponse.getBody()).isNotNull();
        Long restaurantId = createRestaurantResponse.getBody().id();

        ReviewRequestDTO reviewRequest = new ReviewRequestDTO(
                userId,
                restaurantId,
                5,
                "Отличный ресторан!"
        );
        HttpEntity<ReviewRequestDTO> reviewEntity = new HttpEntity<>(reviewRequest, createJsonHeaders());

        ResponseEntity<ReviewResponseDTO> createReviewResponse = restTemplate.exchange(
                "/api/reviews",
                HttpMethod.POST,
                reviewEntity,
                ReviewResponseDTO.class
        );

        assertThat(createReviewResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<RestaurantResponseDTO> getRestaurantResponse = restTemplate.getForEntity(
                "/api/restaurants/" + restaurantId,
                RestaurantResponseDTO.class
        );

        assertThat(getRestaurantResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getRestaurantResponse.getBody()).isNotNull();

        ResponseEntity<UserResponseDTO> getUserResponse = restTemplate.getForEntity(
                "/api/users/" + userId,
                UserResponseDTO.class
        );

        assertThat(getUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getUserResponse.getBody()).isNotNull();
        assertThat(getUserResponse.getBody().name()).isEqualTo("Иван Петров");

        ResponseEntity<List<RestaurantResponseDTO>> getAllRestaurantsResponse = restTemplate.exchange(
                "/api/restaurants",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<RestaurantResponseDTO>>() {}
        );

        assertThat(getAllRestaurantsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllRestaurantsResponse.getBody()).hasSize(1);
    }

    @Test
    void duplicateReview_ShouldBeBlocked() {
        UserRequestDTO userRequest = new UserRequestDTO("Анна Смирнова", 28, "Женский");
        HttpEntity<UserRequestDTO> userEntity = new HttpEntity<>(userRequest, createJsonHeaders());

        ResponseEntity<UserResponseDTO> createUserResponse = restTemplate.exchange(
                "/api/users",
                HttpMethod.POST,
                userEntity,
                UserResponseDTO.class
        );
        Long userId = createUserResponse.getBody().id();

        RestaurantRequestDTO restaurantRequest = new RestaurantRequestDTO(
                "Японский Сад",
                "Лучшие суши в городе",
                "JAPANESE",
                new BigDecimal("3000")
        );
        HttpEntity<RestaurantRequestDTO> restaurantEntity = new HttpEntity<>(restaurantRequest, createJsonHeaders());

        ResponseEntity<RestaurantResponseDTO> createRestaurantResponse = restTemplate.exchange(
                "/api/restaurants",
                HttpMethod.POST,
                restaurantEntity,
                RestaurantResponseDTO.class
        );
        Long restaurantId = createRestaurantResponse.getBody().id();

        ReviewRequestDTO reviewRequest = new ReviewRequestDTO(userId, restaurantId, 5, "Отлично!");
        HttpEntity<ReviewRequestDTO> reviewEntity = new HttpEntity<>(reviewRequest, createJsonHeaders());

        ResponseEntity<ReviewResponseDTO> firstReview = restTemplate.exchange(
                "/api/reviews",
                HttpMethod.POST,
                reviewEntity,
                ReviewResponseDTO.class
        );
        assertThat(firstReview.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<Object> secondReview = restTemplate.exchange(
                "/api/reviews",
                HttpMethod.POST,
                reviewEntity,
                Object.class
        );
        assertThat(secondReview.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getNonExistentEntity_ShouldReturnNotFound() {
        ResponseEntity<Object> userResponse = restTemplate.getForEntity(
                "/api/users/99999",
                Object.class
        );
        assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<Object> restaurantResponse = restTemplate.getForEntity(
                "/api/restaurants/99999",
                Object.class
        );
        assertThat(restaurantResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<Object> reviewResponse = restTemplate.getForEntity(
                "/api/reviews/99999/99999",
                Object.class
        );
        assertThat(reviewResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void validationErrors_ShouldReturnBadRequest() {
        UserRequestDTO invalidUser = new UserRequestDTO("A", 16, "");
        HttpEntity<UserRequestDTO> invalidUserEntity = new HttpEntity<>(invalidUser, createJsonHeaders());

        ResponseEntity<Object> response = restTemplate.exchange(
                "/api/users",
                HttpMethod.POST,
                invalidUserEntity,
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteUserWithReviews_ShouldRecalculateRatings() {
        UserRequestDTO userRequest = new UserRequestDTO("Дмитрий Козлов", 42, "Мужской");
        HttpEntity<UserRequestDTO> userEntity = new HttpEntity<>(userRequest, createJsonHeaders());
        ResponseEntity<UserResponseDTO> userResponse = restTemplate.exchange(
                "/api/users", HttpMethod.POST, userEntity, UserResponseDTO.class
        );
        Long userId = userResponse.getBody().id();

        RestaurantRequestDTO restaurantRequest = new RestaurantRequestDTO(
                "Теремок", "Русская кухня", "RUSSIAN", new BigDecimal("1200")
        );
        HttpEntity<RestaurantRequestDTO> restaurantEntity = new HttpEntity<>(restaurantRequest, createJsonHeaders());
        ResponseEntity<RestaurantResponseDTO> restaurantResponse = restTemplate.exchange(
                "/api/restaurants", HttpMethod.POST, restaurantEntity, RestaurantResponseDTO.class
        );
        Long restaurantId = restaurantResponse.getBody().id();

        ReviewRequestDTO reviewRequest = new ReviewRequestDTO(userId, restaurantId, 5, "Отлично!");
        HttpEntity<ReviewRequestDTO> reviewEntity = new HttpEntity<>(reviewRequest, createJsonHeaders());
        restTemplate.exchange("/api/reviews", HttpMethod.POST, reviewEntity, ReviewResponseDTO.class);

        restaurantResponse = restTemplate.getForEntity("/api/restaurants/" + restaurantId, RestaurantResponseDTO.class);
        assertThat(restaurantResponse.getBody().averageRating()).isEqualByComparingTo(new BigDecimal("5.00"));

        restTemplate.delete("/api/users/" + userId);

        restaurantResponse = restTemplate.getForEntity("/api/restaurants/" + restaurantId, RestaurantResponseDTO.class);
        assertThat(restaurantResponse.getBody().averageRating()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(restaurantResponse.getBody().totalRatings()).isZero();
    }
}