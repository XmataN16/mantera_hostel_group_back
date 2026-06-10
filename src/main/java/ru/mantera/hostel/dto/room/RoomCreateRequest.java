package ru.mantera.hostel.dto.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoomCreateRequest(

        @NotNull(message = "hotelId обязателен")
        Long hotelId,

        @NotNull(message = "roomTypeId обязателен")
        Long roomTypeId,

        @NotBlank(message = "roomNumber обязателен")
        @Size(max = 50)
        String roomNumber,

        Integer floor,

        String comment
) {
}