package ru.mantera.hostel.dto.reservation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ru.mantera.hostel.enums.ReservationSource;

import java.time.LocalDate;
import java.util.List;

public record ReservationCreateRequest(
        @NotNull(message = "hotelId обязателен")
        Long hotelId,

        @NotNull(message = "guestId обязателен")
        Long guestId,

        String reservationNumber,

        @NotNull(message = "source обязателен")
        ReservationSource source,

        @NotNull(message = "checkInDate обязателен")
        LocalDate checkInDate,

        @NotNull(message = "checkOutDate обязателен")
        LocalDate checkOutDate,

        @NotNull(message = "adults обязателен")
        @Positive(message = "adults должен быть больше 0")
        Integer adults,

        @NotNull(message = "children обязателен")
        @PositiveOrZero(message = "children не может быть отрицательным")
        Integer children,

        String comment,

        @NotEmpty(message = "Нужно добавить хотя бы одну комнату в бронирование")
        @Valid
        List<ReservationRoomRequest> rooms
) {
}