package ru.mantera.hostel.dto.availability;

import ru.mantera.hostel.enums.HousekeepingStatus;
import ru.mantera.hostel.enums.RoomStatus;

import java.math.BigDecimal;

public record AvailabilityRoomResponse(
        Long roomId,
        String roomNumber,
        Integer floor,
        Long hotelId,
        String hotelName,
        Long roomTypeId,
        String roomTypeName,
        Integer capacity,
        BigDecimal basePrice,
        RoomStatus roomStatus,
        HousekeepingStatus housekeepingStatus
) {
}