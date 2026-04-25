package com.example.restaurantrating.config;

import com.example.restaurantrating.dto.RestaurantRequestDTO;
import com.example.restaurantrating.dto.ReviewRequestDTO;
import com.example.restaurantrating.dto.UserRequestDTO;
import com.example.restaurantrating.service.RatingService;
import com.example.restaurantrating.service.RestaurantService;
import com.example.restaurantrating.service.VisitorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer {
    private final VisitorService visitorService;
    private final RestaurantService restaurantService;
    private final RatingService ratingService;

    @PostConstruct
    @Transactional
    public void init() {
        System.out.println("\nСИСТЕМА ОЦЕНКИ РЕСТОРАНОВ");

        testAddVisitors();
        testAddRestaurants();
        testAddRatings();
        testShowResultsWithRatings();
        testFindRatingById();

        System.out.println("\nТЕСТИРОВАНИЕ ЗАВЕРШЕНО\n");
    }

    private void testAddVisitors() {
        System.out.println("\nПОСЕТИТЕЛИ");

        visitorService.createUser(new UserRequestDTO("Иван Петров", 30, "Мужской"));
        visitorService.createUser(new UserRequestDTO("Аноним", 25, "Женский"));
        visitorService.createUser(new UserRequestDTO("Сергей Иванов", 35, "Мужской"));
        visitorService.createUser(new UserRequestDTO("Анна Смирнова", 28, "Женский"));
        visitorService.createUser(new UserRequestDTO("Дмитрий Козлов", 42, "Мужской"));
        visitorService.createUser(new UserRequestDTO("Елена Попова", 33, "Женский"));
        visitorService.createUser(new UserRequestDTO("Аноним", 29, "Мужской"));
        visitorService.createUser(new UserRequestDTO("Ольга Новикова", 31, "Женский"));

        System.out.println("Добавлено посетителей: " + visitorService.getAllUsers().size());
        visitorService.getAllUsers().forEach(v ->
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

        var visitors = visitorService.getAllUsers();
        var restaurants = restaurantService.getAllRestaurants();

        if (restaurants.size() < 5) {
            System.out.println("Ошибка: недостаточно ресторанов для добавления оценок");
            return;
        }

        System.out.println("Добавляем оценки для ресторанов:");

        System.out.println("  Ресторан '" + restaurants.getFirst().name() + "':");
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(0).id(), restaurants.getFirst().id(), 5, "Отличная паста!"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(0).name());
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(1).id(), restaurants.getFirst().id(), 4, "Хорошо, но дорого"));
        System.out.println("    + Оценка 4 звезды от " + visitors.get(1).name());
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(2).id(), restaurants.get(2).id(), 5, "Супер место!"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(2).name());

        System.out.println("  Ресторан '" + restaurants.get(1).name() + "':");
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(3).id(), restaurants.get(1).id(), 5, "Лучшие утки по-пекински"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(3).name());
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(4).id(), restaurants.get(1).id(), 4, "Очень вкусно"));
        System.out.println("    + Оценка 4 звезды от " + visitors.get(4).name());

        System.out.println("  Ресторан '" + restaurants.get(2).name() + "':");
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(5).id(), restaurants.get(2).id(), 5, "Отличные роллы"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(5).name());
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(6).id(), restaurants.get(2).id(), 4, "Хорошо"));
        System.out.println("    + Оценка 4 звезды от " + visitors.get(6).name());

        System.out.println("  Ресторан '" + restaurants.get(3).name() + "':");
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(7).id(), restaurants.get(3).id(), 5, "Как у бабушки"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(7).name());
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(0).id(), restaurants.get(3).id(), 4, "Блины отличные"));
        System.out.println("    + Оценка 4 звезды от " + visitors.get(0).name());
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(1).id(), restaurants.get(3).id(), 5, "Борщ - огонь!"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(1).name());

        System.out.println("  Ресторан '" + restaurants.get(4).name() + "':");
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(2).id(), restaurants.get(4).id(), 5, "Изысканно"));
        System.out.println("    + Оценка 5 звезд от " + visitors.get(2).name());
        ratingService.createReview(new ReviewRequestDTO(
                visitors.get(3).id(), restaurants.get(4).id(), 4, "Дорого, но вкусно"));
        System.out.println("    + Оценка 4 звезды от " + visitors.get(3).name());

        System.out.println("\nВсего добавлено оценок: " + ratingService.getAllReviews().size());
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

        var visitors = visitorService.getAllUsers();
        var restaurants = restaurantService.getAllRestaurants();

        if (!visitors.isEmpty() && !restaurants.isEmpty()) {
            Long visitorId = visitors.getFirst().id();
            Long restaurantId = restaurants.getFirst().id();
            System.out.println("Ищем оценку посетителя ID: " + visitorId + " для ресторана ID: " + restaurantId);

            try {
                var foundRating = ratingService.getReviewById(visitorId, restaurantId);
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