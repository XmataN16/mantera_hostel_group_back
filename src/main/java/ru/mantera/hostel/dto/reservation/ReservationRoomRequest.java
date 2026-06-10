package ru.mantera.hostel.dto.reservation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ReservationRoomRequest(
        @NotNull(message = "roomTypeId обязателен")
        Long roomTypeId,

        Long roomId,

        Long ratePlanId,

        @NotNull(message = "guestsCount обязателен")
        @Positive(message = "guestsCount должен быть больше 0")
        Integer guestsCount,

        @PositiveOrZero(message = "pricePerNight не может быть отрицательным")
        BigDecimal pricePerNight
) {
}