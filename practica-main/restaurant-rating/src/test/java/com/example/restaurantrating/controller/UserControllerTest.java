package com.example.restaurantrating.controller;

import com.example.restaurantrating.dto.UserRequestDTO;
import com.example.restaurantrating.dto.UserResponseDTO;
import com.example.restaurantrating.model.User;
import com.example.restaurantrating.repository.RestaurantRepository;
import com.example.restaurantrating.repository.ReviewRepository;
import com.example.restaurantrating.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends BaseControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        userRepository.deleteAll();
        restaurantRepository.deleteAll();
    }

    @Test
    void createUser_WithValidData_ShouldReturnCreated() throws Exception {
        UserRequestDTO request = new UserRequestDTO("Тестовый Пользователь", 30, "Мужской");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Тестовый Пользователь"))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.gender").value("Мужской"))
                .andReturn();

        UserResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                UserResponseDTO.class
        );
        assertThat(response.id()).isNotNull();
    }

    @Test
    void createUser_WithInvalidName_ShouldReturnBadRequest() throws Exception {
        UserRequestDTO request = new UserRequestDTO("A", 30, "Мужской");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void createUser_WithAgeBelow18_ShouldReturnBadRequest() throws Exception {
        UserRequestDTO request = new UserRequestDTO("Тестовый", 16, "Мужской");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.age").exists());
    }

    @Test
    void createUser_WithEmptyGender_ShouldReturnBadRequest() throws Exception {
        UserRequestDTO request = new UserRequestDTO("Тестовый", 30, "");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.gender").exists());
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        createUserInDb("Иван", 30, "Мужской");
        createUserInDb("Анна", 25, "Женский");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Иван"))
                .andExpect(jsonPath("$[1].name").value("Анна"));
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUserById_WhenExists_ShouldReturnUser() throws Exception {
        User savedUser = createUserInDb("Петр", 35, "Мужской");

        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("Петр"))
                .andExpect(jsonPath("$.age").value(35));
    }

    @Test
    void getUserById_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("не найден")));
    }

    @Test
    void updateUser_WhenExists_ShouldUpdate() throws Exception {
        User savedUser = createUserInDb("СтароеИмя", 30, "Мужской");
        UserRequestDTO updateRequest = new UserRequestDTO("НовоеИмя", 35, "Женский");

        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("НовоеИмя"))
                .andExpect(jsonPath("$.age").value(35))
                .andExpect(jsonPath("$.gender").value("Женский"));
    }

    @Test
    void updateUser_WhenNotExists_ShouldReturnNotFound() throws Exception {
        UserRequestDTO updateRequest = new UserRequestDTO("Имя", 30, "Мужской");

        mockMvc.perform(put("/api/users/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_WhenExists_ShouldDelete() throws Exception {
        User savedUser = createUserInDb("ДляУдаления", 30, "Мужской");

        mockMvc.perform(delete("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    void deleteUser_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    private User createUserInDb(String name, int age, String gender) {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setGender(gender);
        return userRepository.save(user);
    }
}