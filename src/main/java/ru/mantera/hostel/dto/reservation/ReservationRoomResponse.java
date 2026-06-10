package ru.mantera.hostel.dto.reservation;

import ru.mantera.hostel.enums.ReservationRoomStatus;

import java.math.BigDecimal;

public record ReservationRoomResponse(
        Long id,
        Long roomTypeId,
        Long roomId,
        Long ratePlanId,
        Integer guestsCount,
        BigDecimal pricePerNight,
        ReservationRoomStatus status
) {}