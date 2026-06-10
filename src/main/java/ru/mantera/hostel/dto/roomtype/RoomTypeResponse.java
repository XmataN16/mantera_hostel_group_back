package ru.mantera.hostel.dto.roomtype;

import ru.mantera.hostel.enums.HotelStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record RoomTypeResponse(
        Long id,
        Long hotelId,
        String hotelName,
        String code,
        String name,
        String description,
        Integer capacity,
        BigDecimal basePrice,
        HotelStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}