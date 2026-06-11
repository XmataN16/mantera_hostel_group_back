package ru.mantera.hostel.dto.auth;

import java.util.List;

public record JwtLoginResponse(
        String tokenType,
        String accessToken,
        String username,
        List<String> roles
) {
}