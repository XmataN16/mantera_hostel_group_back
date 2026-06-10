package ru.mantera.hostel.dto.room;

import ru.mantera.hostel.enums.HousekeepingStatus;
import ru.mantera.hostel.enums.RoomStatus;

import java.time.OffsetDateTime;

public record RoomResponse(
        Long id,
        Long hotelId,
        String hotelName,
        Long roomTypeId,
        String roomTypeName,
        String roomNumber,
        Integer floor,
        RoomStatus status,
        HousekeepingStatus housekeepingStatus,
        String comment,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}