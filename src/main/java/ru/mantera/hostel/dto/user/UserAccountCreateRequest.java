package ru.mantera.hostel.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserAccountCreateRequest(

        @NotNull(message = "employeeId обязателен")
        Long employeeId,

        @NotBlank(message = "username обязателен")
        @Size(max = 100)
        String username,

        @NotBlank(message = "password обязателен")
        @Size(min = 6, max = 100)
        String password,

        @NotEmpty(message = "Нужно указать хотя бы одну роль")
        Set<String> roleCodes
) {
}