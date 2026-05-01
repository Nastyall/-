package com.example.restaurantrating.service;

import com.example.restaurantrating.dto.UserRequestDTO;
import com.example.restaurantrating.dto.UserResponseDTO;
import com.example.restaurantrating.model.Review;
import com.example.restaurantrating.model.User;
import com.example.restaurantrating.repository.ReviewRepository;
import com.example.restaurantrating.repository.UserRepository;
import com.example.restaurantrating.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createUser(1L, "Иван Петров", 30, "Мужской");
        validRequest = TestDataFactory.createValidUserRequest();
    }

    @Test
    void createUser_WithValidData_ShouldReturnUserResponse() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDTO result = userService.createUser(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Иван Петров");
        assertThat(result.age()).isEqualTo(30);
        assertThat(result.gender()).isEqualTo("Мужской");

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponseDTO result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Иван Петров");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        List<User> users = List.of(
                testUser,
                TestDataFactory.createUser(2L, "Анна", 25, "Женский")
        );
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserResponseDTO::name)
                .containsExactlyInAnyOrder("Иван Петров", "Анна");

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).isEmpty();

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAndReturnUser() {
        UserRequestDTO updateRequest = new UserRequestDTO("Иван Петров Обновленный", 31, "Мужской");
        User updatedUser = TestDataFactory.createUser(1L, "Иван Петров Обновленный", 31, "Мужской");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponseDTO result = userService.updateUser(1L, updateRequest);

        assertThat(result.name()).isEqualTo("Иван Петров Обновленный");
        assertThat(result.age()).isEqualTo(31);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, validRequest))
                .isInstanceOf(RuntimeException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByVisitorId(1L)).thenReturn(new ArrayList<>());

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");

        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteUser_WithReviews_ShouldDeleteUserAndRecalculateRatings() {
        List<Review> userReviews = List.of(
                TestDataFactory.createReview(1L, 10L, 4, "Good"),
                TestDataFactory.createReview(1L, 20L, 5, "Excellent")
        );

        when(userRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByVisitorId(1L)).thenReturn(userReviews);
        when(reviewRepository.findByRestaurantId(10L)).thenReturn(new ArrayList<>());
        when(reviewRepository.findByRestaurantId(20L)).thenReturn(new ArrayList<>());

        userService.deleteUser(1L);

        verify(reviewRepository, times(1)).deleteAll(anyList());
        verify(restaurantService, times(2)).recalculateAverageRating(anyLong(), anyList());
        verify(userRepository, times(1)).deleteById(1L);
    }
}