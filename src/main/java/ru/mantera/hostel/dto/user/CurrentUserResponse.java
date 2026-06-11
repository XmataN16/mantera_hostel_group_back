package ru.mantera.hostel.dto.user;

import java.util.List;

public record CurrentUserResponse(
        String username,
        List<String> roles
) {
}