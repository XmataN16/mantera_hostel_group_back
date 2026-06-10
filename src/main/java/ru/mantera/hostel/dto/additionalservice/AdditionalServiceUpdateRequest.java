package ru.mantera.hostel.dto.additionalservice;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ru.mantera.hostel.enums.ServiceStatus;

import java.math.BigDecimal;

public record AdditionalServiceUpdateRequest(

        @Size(max = 150)
        String name,

        String description,

        @PositiveOrZero(message = "price не может быть отрицательным")
        BigDecimal price,

        ServiceStatus status
) {
}