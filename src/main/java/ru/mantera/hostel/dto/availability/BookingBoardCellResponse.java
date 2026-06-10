package ru.mantera.hostel.dto.availability;

import java.time.LocalDate;

public record BookingBoardCellResponse(
        Long roomId,
        String roomNumber,
        LocalDate date,
        String status,
        Long reservationId,
        String reservationNumber,
        Long guestId
) {
}