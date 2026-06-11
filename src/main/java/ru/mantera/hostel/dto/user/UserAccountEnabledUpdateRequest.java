package ru.mantera.hostel.dto.user;

import jakarta.validation.constraints.NotNull;

public record UserAccountEnabledUpdateRequest(

        @NotNull(message = "enabled обязателен")
        Boolean enabled
) {
}