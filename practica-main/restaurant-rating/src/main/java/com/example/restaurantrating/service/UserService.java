package com.example.restaurantrating.service;

import com.example.restaurantrating.dto.UserRequestDTO;
import com.example.restaurantrating.dto.UserResponseDTO;
import com.example.restaurantrating.model.Review;
import com.example.restaurantrating.model.User;
import com.example.restaurantrating.repository.ReviewRepository;
import com.example.restaurantrating.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final RestaurantService restaurantService;

    public UserResponseDTO createUser(@Valid UserRequestDTO request) {
        User user = new User();
        user.setName(request.name());
        user.setAge(request.age());
        user.setGender(request.gender());
        User saved = userRepository.save(user);
        return new UserResponseDTO(
                saved.getId(),
                saved.getName(),
                saved.getAge(),
                saved.getGender()
        );
    }

    public UserResponseDTO updateUser(Long id, @Valid UserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Посетитель с ID " + id + " не найден"));
        user.setName(request.name());
        user.setAge(request.age());
        user.setGender(request.gender());
        User updated = userRepository.save(user);
        return new UserResponseDTO(
                updated.getId(),
                updated.getName(),
                updated.getAge(),
                updated.getGender()
        );
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Посетитель с ID " + id + " не найден");
        }
        List<Review> reviews = reviewRepository.findByVisitorId(id);
        List<Long> affectedRestaurantIds = reviews.stream()
                .map(Review::getRestaurantId)
                .distinct()
                .collect(Collectors.toList());

        reviewRepository.deleteAll(reviews);

        affectedRestaurantIds.forEach(restaurantId -> {
            List<Review> restaurantReviews = reviewRepository.findByRestaurantId(restaurantId);
            List<Integer> ratingValues = restaurantReviews.stream()
                    .map(Review::getRating)
                    .collect(Collectors.toList());
            restaurantService.recalculateAverageRating(restaurantId, ratingValues);
        });

        userRepository.deleteById(id);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(v -> new UserResponseDTO(
                        v.getId(),
                        v.getName(),
                        v.getAge(),
                        v.getGender()
                ))
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Посетитель с ID " + id + " не найден"));
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getAge(),
                user.getGender()
        );
    }
}