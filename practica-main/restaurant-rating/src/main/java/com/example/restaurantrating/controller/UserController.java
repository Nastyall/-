package com.example.restaurantrating.controller;

import com.example.restaurantrating.dto.UserRequestDTO;
import com.example.restaurantrating.dto.UserResponseDTO;
import com.example.restaurantrating.service.VisitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Посетители", description = "Управление посетителями ресторанов")
public class UserController {
    private final VisitorService visitorService;

    @PostMapping
    @Operation(summary = "Создать нового посетителя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Посетитель успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO user = visitorService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    @Operation(summary = "Получить всех посетителей")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(visitorService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить посетителя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель найден"),
            @ApiResponse(responseCode = "404", description = "Посетитель не найден")
    })
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID посетителя", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(visitorService.getUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные посетителя")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(visitorService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить посетителя")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = visitorService.deleteUser(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}