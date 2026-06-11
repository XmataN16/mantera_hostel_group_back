package ru.mantera.hostel.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserAccountPasswordUpdateRequest(

        @NotBlank(message = "newPassword обязателен")
        @Size(min = 6, max = 100)
        String newPassword
) {
}