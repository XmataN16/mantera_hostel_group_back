package ru.mantera.hostel.dto.roomtype;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RoomTypeCreateRequest(

        @NotNull(message = "hotelId обязателен")
        Long hotelId,

        @NotBlank(message = "code обязателен")
        @Size(max = 50)
        String code,

        @NotBlank(message = "name обязателен")
        @Size(max = 120)
        String name,

        String description,

        @NotNull
        @Positive(message = "capacity должен быть больше 0")
        Integer capacity,

        @NotNull
        @PositiveOrZero(message = "basePrice не может быть отрицательным")
        BigDecimal basePrice
) {
}