package ru.mantera.hostel.dto.user;

import jakarta.validation.constraints.Size;

public record UserAccountUpdateRequest(

        @Size(max = 100)
        String username,

        Boolean enabled
) {
}