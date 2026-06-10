package ru.mantera.hostel.dto.rate;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RoomTypeRateUpdateRequest(
        LocalDate validFrom,
        LocalDate validTo,

        @PositiveOrZero(message = "price не может быть отрицательным")
        BigDecimal price
) {
}