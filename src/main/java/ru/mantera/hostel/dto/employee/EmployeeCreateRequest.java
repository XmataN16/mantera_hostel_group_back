package ru.mantera.hostel.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EmployeeCreateRequest(

        @NotNull(message = "hotelId обязателен")
        Long hotelId,

        @NotBlank(message = "fullName обязателен")
        @Size(max = 255)
        String fullName,

        @NotBlank(message = "position обязателен")
        @Size(max = 120)
        String position,

        @Size(max = 50)
        String phone,

        @Email
        @Size(max = 255)
        String email
) {
}