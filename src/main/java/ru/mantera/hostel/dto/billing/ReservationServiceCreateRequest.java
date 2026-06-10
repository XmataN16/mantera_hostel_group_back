package ru.mantera.hostel.dto.billing;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationServiceCreateRequest(

        @NotNull(message = "serviceId обязателен")
        Long serviceId,

        @NotNull(message = "quantity обязателен")
        @Positive(message = "quantity должен быть больше 0")
        Integer quantity
) {
}