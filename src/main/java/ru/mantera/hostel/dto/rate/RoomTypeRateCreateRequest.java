package ru.mantera.hostel.dto.rate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RoomTypeRateCreateRequest(

        @NotNull(message = "roomTypeId обязателен")
        Long roomTypeId,

        @NotNull(message = "ratePlanId обязателен")
        Long ratePlanId,

        @NotNull(message = "validFrom обязателен")
        LocalDate validFrom,

        @NotNull(message = "validTo обязателен")
        LocalDate validTo,

        @NotNull(message = "price обязателен")
        @PositiveOrZero(message = "price не может быть отрицательным")
        BigDecimal price
) {
}