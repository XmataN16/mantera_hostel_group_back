package ru.mantera.hostel.dto.guest;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record GuestResponse(
        Long id,
        String lastName,
        String firstName,
        String middleName,
        LocalDate birthDate,
        String gender,
        String phone,
        String email,
        String citizenship,
        String documentType,
        String documentNumber,
        LocalDate documentIssueDate,
        String documentIssuedBy,
        String address,
        String comment,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}