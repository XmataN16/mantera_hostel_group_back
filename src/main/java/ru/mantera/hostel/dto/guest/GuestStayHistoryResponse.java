package ru.mantera.hostel.dto.guest;

import ru.mantera.hostel.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GuestStayHistoryResponse(
        Long reservationId,
        String reservationNumber,
        Long hotelId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        ReservationStatus status,
        BigDecimal totalAmount
) {
}