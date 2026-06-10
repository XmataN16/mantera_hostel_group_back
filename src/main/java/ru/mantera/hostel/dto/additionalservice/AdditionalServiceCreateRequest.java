package ru.mantera.hostel.dto.additionalservice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AdditionalServiceCreateRequest(

        @NotNull(message = "hotelId обязателен")
        Long hotelId,

        @NotBlank(message = "name обязателен")
        @Size(max = 150)
        String name,

        String description,

        @NotNull(message = "price обязателен")
        @PositiveOrZero(message = "price не может быть отрицательным")
        BigDecimal price
) {
}