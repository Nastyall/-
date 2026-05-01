package com.example.restaurantrating.config;

import com.example.restaurantrating.dto.RestaurantRequestDTO;
import com.example.restaurantrating.dto.ReviewRequestDTO;
import com.example.restaurantrating.dto.UserRequestDTO;
import com.example.restaurantrating.service.ReviewService;
import com.example.restaurantrating.service.RestaurantService;
import com.example.restaurantrating.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final ReviewService reviewService;

    @PostConstruct
    public void init() {
        try {
            System.out.println("\nСИСТЕМА ОЦЕНКИ РЕСТОРАНОВ");

            testAddVisitors();
            testAddRestaurants();
            testAddRatings();
            testShowResultsWithRatings();
            testFindRatingById();

            System.out.println("\nТЕСТИРОВАНИЕ ЗАВЕРШЕНО\n");
        } catch (Exception e) {
            System.err.println("Ошибка инициализации данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testAddVisitors() {
        System.out.println("\nПОСЕТИТЕЛИ");

        userService.createUser(new UserRequestDTO("Иван Петров", 30, "Мужской"));
        userService.createUser(new UserRequestDTO("Аноним", 25, "Женский"));
        userService.createUser(new UserRequestDTO("Сергей Иванов", 35, "Мужской"));
        userService.createUser(new UserRequestDTO("Анна Смирнова", 28, "Женский"));
        userService.createUser(new UserRequestDTO("Дмитрий Козлов", 42, "Мужской"));
        userService.createUser(new UserRequestDTO("Елена Попова", 33, "Женский"));
        userService.createUser(new UserRequestDTO("Аноним", 29, "Мужской"));
        userService.createUser(new UserRequestDTO("Ольга Новикова", 31, "Женский"));

        System.out.println("Добавлено посетителей: " + userService.getAllUsers().size());
        userService.getAllUsers().forEach(v ->
                System.out.println("  ID:" + v.id() + " | Имя: " + v.name() +
                        " | Возраст: " + v.age() + " | Пол: " + v.gender()));
    }

    private void testAddRestaurants() {
        System.out.println("\nРЕСТОРАНЫ");

        restaurantService.createRestaurant(new RestaurantRequestDTO(
                "Белые ночи", "Уютный ресторан итальянской кухни", "ITALIAN", new BigDecimal("2500")));
        restaurantService.createRestaurant(new RestaurantRequestDTO(
                "Старый город", "Аутентичная китайская кухня", "CHINESE", new BigDecimal("1500")));
        restaurantService.createRestaurant(new RestaurantRequestDTO(
                "У камина", "Японский ресторан с суши-баром", "JAPANESE", new BigDecimal("2000")));
        restaurantService.createRestaurant(new RestaurantRequestDTO(
                "Теремок", "Традиционная русская кухня", "RUSSIAN", new BigDecimal("1200")));
        restaurantService.createRestaurant(new RestaurantRequestDTO(
                "Rene", "Изысканная французская кухня", "FRENCH", new BigDecimal("3500")));

        System.out.println("Добавлено ресторанов: " + restaurantService.getAllRestaurants().size());
        restaurantService.getAllRestaurants().forEach(r ->
                System.out.println("  ID:" + r.id() + " | " + r.name() +
                        " | Кухня: " + r.cuisineType() +
                        " | Средний чек: " + r.averageBill() + " руб."));
    }

    private void testAddRatings() {
        System.out.println("\nОЦЕНКИ");

        var visitors = userService.getAllUsers();
        var restaurants = restaurantService.getAllRestaurants();

        if (restaurants.size() < 5) {
            System.out.println("Ошибка: недостаточно ресторанов для добавления оценок");
            return;
        }

        System.out.println("Добавляем оценки для ресторанов:");

        System.out.println("  Ресторан '" + restaurants.get(0).name() + "':");
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(0).id(), restaurants.get(0).id(), 5, "Отличная паста!"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(0).name());
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(1).id(), restaurants.get(0).id(), 4, "Хорошо, но дорого"));
        System.out.println("    + Оценка 4 звезды от " + visitors.get(1).name());
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(2).id(), restaurants.get(2).id(), 5, "Супер место!"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(2).name());

        System.out.println("  Ресторан '" + restaurants.get(1).name() + "':");
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(3).id(), restaurants.get(1).id(), 5, "Лучшие утки по-пекински"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(3).name());
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(4).id(), restaurants.get(1).id(), 4, "Очень вкусно"));
        System.out.println("    + Оценка 4 звезды от " + visitors.get(4).name());

        System.out.println("  Ресторан '" + restaurants.get(2).name() + "':");
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(5).id(), restaurants.get(2).id(), 5, "Отличные роллы"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(5).name());
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(6).id(), restaurants.get(2).id(), 4, "Хорошо"));
        System.out.println("    + Оценка 4 звезды от " + visitors.get(6).name());

        System.out.println("  Ресторан '" + restaurants.get(3).name() + "':");
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(7).id(), restaurants.get(3).id(), 5, "Как у бабушки"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(7).name());
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(0).id(), restaurants.get(3).id(), 4, "Блины отличные"));
        System.out.println("    + Оценка 4 звезды от " + visitors.get(0).name());
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(1).id(), restaurants.get(3).id(), 5, "Борщ - огонь!"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(1).name());

        System.out.println("  Ресторан '" + restaurants.get(4).name() + "':");
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(2).id(), restaurants.get(4).id(), 5, "Изысканно"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(2).name());
        reviewService.createReview(new ReviewRequestDTO(
                visitors.get(3).id(), restaurants.get(4).id(), 4, "Дорого, но вкусно"));
        System.out.println("    + Оценка 4 звезды от " + visitors.get(3).name());

        System.out.println("\nВсего добавлено оценок: " + reviewService.getAllReviews().size());
    }

    private void testShowResultsWithRatings() {
        System.out.println("\nРЕЗУЛЬТАТ С ОЦЕНКАМИ");

        restaurantService.getAllRestaurants().forEach(r -> {
            System.out.println("\n  Ресторан: " + r.name());
            System.out.println("     Кухня: " + r.cuisineType());
            System.out.println("     Средний чек: " + r.averageBill() + " руб.");
            System.out.println("     СРЕДНЯЯ ОЦЕНКА: " + r.averageRating() + " из 5");
        });
    }

    private void testFindRatingById() {
        System.out.println("\nТЕСТИРОВАНИЕ RatingService.getReviewById()");

        var visitors = userService.getAllUsers();
        var restaurants = restaurantService.getAllRestaurants();

        if (!visitors.isEmpty() && !restaurants.isEmpty()) {
            Long visitorId = visitors.get(0).id();
            Long restaurantId = restaurants.get(0).id();
            System.out.println("Ищем оценку посетителя ID: " + visitorId + " для ресторана ID: " + restaurantId);

            try {
                var foundRating = reviewService.getReviewById(visitorId, restaurantId);
                System.out.println("Оценка найдена:");
                System.out.println("  Посетитель ID: " + foundRating.visitorId());
                System.out.println("  Ресторан ID: " + foundRating.restaurantId());
                System.out.println("  Оценка: " + foundRating.rating() + " звезд");
                System.out.println("  Отзыв: " + foundRating.reviewText());
            } catch (RuntimeException e) {
                System.out.println("Оценка не найдена");
            }
        }
    }
}