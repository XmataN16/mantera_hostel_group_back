package ru.mantera.hostel.dto.employee;

import ru.mantera.hostel.enums.EmployeeStatus;

import java.time.OffsetDateTime;

public record EmployeeResponse(
        Long id,
        Long hotelId,
        String hotelName,
        String fullName,
        String position,
        String phone,
        String email,
        EmployeeStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}