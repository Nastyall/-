package com.example.restaurantrating.service;

import com.example.restaurantrating.dto.ReviewRequestDTO;
import com.example.restaurantrating.dto.ReviewResponseDTO;
import com.example.restaurantrating.model.Review;
import com.example.restaurantrating.repository.ReviewRepository;
import com.example.restaurantrating.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReviewService reviewService;

    private Review testReview;
    private ReviewRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        testReview = TestDataFactory.createReview(1L, 1L, 5, "Отличный ресторан!");
        validRequest = TestDataFactory.createValidReviewRequest(1L, 1L);
    }

    @Test
    void createReview_WhenValid_ShouldCreateReview() {
        when(userService.getUserById(1L)).thenReturn(null);
        when(restaurantService.getRestaurantById(1L)).thenReturn(null);
        when(reviewRepository.existsByVisitorIdAndRestaurantId(1L, 1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
        when(reviewRepository.findByRestaurantId(1L)).thenReturn(List.of(testReview));

        ReviewResponseDTO result = reviewService.createReview(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.reviewText()).isEqualTo("Отличный ресторан!");

        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(restaurantService, times(1)).recalculateAverageRating(eq(1L), anyList());
    }

    @Test
    void createReview_WhenUserNotExists_ShouldThrowException() {
        doThrow(new RuntimeException("User not found"))
                .when(userService).getUserById(1L);

        assertThatThrownBy(() -> reviewService.createReview(validRequest))
                .isInstanceOf(RuntimeException.class);

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void createReview_WhenReviewAlreadyExists_ShouldThrowException() {
        when(userService.getUserById(1L)).thenReturn(null);
        when(restaurantService.getRestaurantById(1L)).thenReturn(null);
        when(reviewRepository.existsByVisitorIdAndRestaurantId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("уже оставил отзыв");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void getReviewById_WhenExists_ShouldReturnReview() {
        when(reviewRepository.findByVisitorIdAndRestaurantId(1L, 1L))
                .thenReturn(Optional.of(testReview));

        ReviewResponseDTO result = reviewService.getReviewById(1L, 1L);

        assertThat(result.visitorId()).isEqualTo(1L);
        assertThat(result.restaurantId()).isEqualTo(1L);
        assertThat(result.rating()).isEqualTo(5);
    }

    @Test
    void getReviewById_WhenNotExists_ShouldThrowException() {
        when(reviewRepository.findByVisitorIdAndRestaurantId(999L, 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(999L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getReviewsByRestaurant_ShouldReturnList() {
        List<Review> reviews = List.of(
                testReview,
                TestDataFactory.createReview(2L, 1L, 4, "Хорошо")
        );
        when(reviewRepository.findByRestaurantId(1L)).thenReturn(reviews);

        List<ReviewResponseDTO> result = reviewService.getReviewsByRestaurant(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ReviewResponseDTO::rating)
                .containsExactlyInAnyOrder(5, 4);
    }

    @Test
    void getReviewsByRestaurantPaged_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(List.of(testReview), pageable, 1);

        when(reviewRepository.findByRestaurantId(1L, pageable))
                .thenReturn(reviewPage);

        Page<ReviewResponseDTO> result = reviewService.getReviewsByRestaurantPaged(1L, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getReviewsByRestaurantSorted_Ascending_ShouldReturnSorted() {
        List<Review> reviews = List.of(
                TestDataFactory.createReview(1L, 1L, 3, "Normal"),
                TestDataFactory.createReview(2L, 1L, 5, "Excellent")
        );
        when(reviewRepository.findByRestaurantIdOrderByRatingAsc(1L))
                .thenReturn(reviews);

        List<ReviewResponseDTO> result = reviewService.getReviewsByRestaurantSorted(1L, "asc");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).rating()).isEqualTo(3);
        assertThat(result.get(1).rating()).isEqualTo(5);
    }

    @Test
    void getReviewsByRestaurantSorted_Descending_ShouldReturnSorted() {
        List<Review> reviews = List.of(
                TestDataFactory.createReview(1L, 1L, 5, "Excellent"),
                TestDataFactory.createReview(2L, 1L, 3, "Normal")
        );
        when(reviewRepository.findByRestaurantIdOrderByRatingDesc(1L))
                .thenReturn(reviews);

        List<ReviewResponseDTO> result = reviewService.getReviewsByRestaurantSorted(1L, "desc");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).rating()).isEqualTo(5);
        assertThat(result.get(1).rating()).isEqualTo(3);
    }

    @Test
    void updateReview_WhenExists_ShouldUpdate() {
        ReviewRequestDTO updateRequest = new ReviewRequestDTO(1L, 1L, 4, "Обновленный отзыв");
        Review updatedReview = TestDataFactory.createReview(1L, 1L, 4, "Обновленный отзыв");

        when(reviewRepository.findByVisitorIdAndRestaurantId(1L, 1L))
                .thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);
        when(reviewRepository.findByRestaurantId(1L)).thenReturn(List.of(updatedReview));

        ReviewResponseDTO result = reviewService.updateReview(1L, 1L, updateRequest);

        assertThat(result.rating()).isEqualTo(4);
        assertThat(result.reviewText()).isEqualTo("Обновленный отзыв");

        verify(restaurantService, times(1)).recalculateAverageRating(eq(1L), anyList());
    }

    @Test
    void deleteReview_WhenExists_ShouldDelete() {
        when(reviewRepository.existsByVisitorIdAndRestaurantId(1L, 1L)).thenReturn(true);
        when(reviewRepository.findByRestaurantId(1L)).thenReturn(new ArrayList<>());

        boolean result = reviewService.deleteReview(1L, 1L);

        assertThat(result).isTrue();
        verify(reviewRepository, times(1)).deleteById(any());
        verify(restaurantService, times(1)).recalculateAverageRating(eq(1L), anyList());
    }

    @Test
    void deleteReview_WhenNotExists_ShouldReturnFalse() {
        when(reviewRepository.existsByVisitorIdAndRestaurantId(999L, 1L)).thenReturn(false);

        boolean result = reviewService.deleteReview(999L, 1L);

        assertThat(result).isFalse();
        verify(reviewRepository, never()).deleteById(any());
    }
}