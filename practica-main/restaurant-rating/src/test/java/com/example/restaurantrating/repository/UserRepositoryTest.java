package com.example.restaurantrating.repository;

import com.example.restaurantrating.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void saveAndFindUser_ShouldWork() {
        User user = new User();
        user.setName("Тестовый");
        user.setAge(30);
        user.setGender("Мужской");

        User saved = userRepository.save(user);
        User found = userRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Тестовый");
        assertThat(found.getAge()).isEqualTo(30);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        userRepository.save(createUser("User1", 25, "Мужской"));
        userRepository.save(createUser("User2", 30, "Женский"));

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2);
    }

    private User createUser(String name, int age, String gender) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setGender(gender);
        return user;
    }
}