package ru.mantera.hostel.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import ru.mantera.hostel.enums.EmployeeStatus;

public record EmployeeUpdateRequest(

        Long hotelId,

        @Size(max = 255)
        String fullName,

        @Size(max = 120)
        String position,

        @Size(max = 50)
        String phone,

        @Email
        @Size(max = 255)
        String email,

        EmployeeStatus status
) {
}