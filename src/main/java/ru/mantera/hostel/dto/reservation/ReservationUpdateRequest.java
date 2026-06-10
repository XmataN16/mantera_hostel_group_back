package ru.mantera.hostel.dto.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.mantera.hostel.enums.ReservationSource;

import java.time.LocalDate;
import java.util.List;

public record ReservationUpdateRequest(
        Long hotelId,
        Long guestId,
        ReservationSource source,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        @Positive(message = "adults должен быть больше 0")
        Integer adults,
        @PositiveOrZero(message = "children не может быть отрицательным")
        Integer children,
        String comment,
        @Valid
        List<ReservationRoomRequest> rooms
) {}