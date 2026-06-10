package ru.mantera.hostel.dto.reservation;

import ru.mantera.hostel.enums.ReservationSource;
import ru.mantera.hostel.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ReservationResponse(
        Long id,
        Long hotelId,
        Long guestId,
        String reservationNumber,
        ReservationSource source,
        ReservationStatus status,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer adults,
        Integer children,
        BigDecimal totalAmount,
        String comment,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<ReservationRoomResponse> rooms
) {}