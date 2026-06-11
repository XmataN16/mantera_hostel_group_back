package ru.mantera.hostel.dto.user;

import java.time.OffsetDateTime;
import java.util.List;

public record UserAccountResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String username,
        Boolean enabled,
        List<String> roles,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}