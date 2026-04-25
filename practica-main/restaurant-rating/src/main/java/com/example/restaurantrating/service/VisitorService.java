package com.example.restaurantrating.service;

import com.example.restaurantrating.dto.UserRequestDTO;
import com.example.restaurantrating.dto.UserResponseDTO;
import com.example.restaurantrating.model.Visitor;
import com.example.restaurantrating.repository.RatingRepository;
import com.example.restaurantrating.repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VisitorService {
    private final VisitorRepository visitorRepository;
    private final RatingRepository ratingRepository;
    private final RestaurantService restaurantService;

    public UserResponseDTO createUser(@Valid UserRequestDTO request) {
        Visitor visitor = new Visitor();
        visitor.setName(request.name());
        visitor.setAge(request.age());
        visitor.setGender(request.gender());
        Visitor saved = visitorRepository.save(visitor);
        return new UserResponseDTO(
                saved.getId(),
                saved.getName(),
                saved.getAge(),
                saved.getGender()
        );
    }

    public UserResponseDTO updateUser(Long id, @Valid UserRequestDTO request) {
        Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Посетитель с ID " + id + " не найден"));
        visitor.setName(request.name());
        visitor.setAge(request.age());
        visitor.setGender(request.gender());
        Visitor updated = visitorRepository.save(visitor);
        return new UserResponseDTO(
                updated.getId(),
                updated.getName(),
                updated.getAge(),
                updated.getGender()
        );
    }

    public boolean deleteUser(Long id) {
        if (visitorRepository.existsById(id)) {
            List<com.example.restaurantrating.model.Rating> ratings = ratingRepository.findByVisitorId(id);
            for (com.example.restaurantrating.model.Rating rating : ratings) {
                Long restaurantId = rating.getRestaurantId();
                ratingRepository.delete(rating);

                List<com.example.restaurantrating.model.Rating> restaurantRatings = ratingRepository.findByRestaurantId(restaurantId);
                List<Integer> ratingValues = restaurantRatings.stream()
                        .map(com.example.restaurantrating.model.Rating::getRating)
                        .collect(Collectors.toList());
                restaurantService.recalculateAverageRating(restaurantId, ratingValues);
            }
            visitorRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<UserResponseDTO> getAllUsers() {
        return visitorRepository.findAll().stream()
                .map(v -> new UserResponseDTO(
                        v.getId(),
                        v.getName(),
                        v.getAge(),
                        v.getGender()
                ))
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Посетитель с ID " + id + " не найден"));
        return new UserResponseDTO(
                visitor.getId(),
                visitor.getName(),
                visitor.getAge(),
                visitor.getGender()
        );
    }
}