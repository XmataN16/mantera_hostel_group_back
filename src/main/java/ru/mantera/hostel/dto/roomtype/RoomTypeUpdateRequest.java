package ru.mantera.hostel.dto.roomtype;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import ru.mantera.hostel.enums.HotelStatus;

import java.math.BigDecimal;

public record RoomTypeUpdateRequest(

        @Size(max = 50)
        String code,

        @Size(max = 120)
        String name,

        String description,

        @Positive
        Integer capacity,

        @PositiveOrZero
        BigDecimal basePrice,

        HotelStatus status
) {
}