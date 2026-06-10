package ru.mantera.hostel.dto.guest;

import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record GuestUpdateRequest(

        String lastName,

        String firstName,

        String middleName,

        LocalDate birthDate,

        String gender,

        String phone,

        @Email
        String email,

        String citizenship,

        String documentType,

        String documentNumber,

        LocalDate documentIssueDate,

        String documentIssuedBy,

        String address,

        String comment
) {
}