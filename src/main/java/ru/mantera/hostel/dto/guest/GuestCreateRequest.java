package ru.mantera.hostel.dto.guest;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record GuestCreateRequest(

        @NotBlank
        String lastName,

        @NotBlank
        String firstName,

        String middleName,

        @NotNull
        LocalDate birthDate,

        @NotBlank
        String gender,

        String phone,

        @Email
        String email,

        String citizenship,

        @NotBlank
        String documentType,

        @NotBlank
        String documentNumber,

        LocalDate documentIssueDate,

        String documentIssuedBy,

        String address,

        String comment
) {
}