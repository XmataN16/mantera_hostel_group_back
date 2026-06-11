package ru.mantera.hostel.dto.user;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UserAccountRolesUpdateRequest(

        @NotEmpty(message = "Нужно указать хотя бы одну роль")
        Set<String> roleCodes
) {
}