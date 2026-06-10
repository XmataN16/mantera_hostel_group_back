package ru.mantera.hostel.dto.room;

import jakarta.validation.constraints.NotNull;
import ru.mantera.hostel.enums.RoomStatus;

public record RoomStatusUpdateRequest(
        @NotNull
        RoomStatus status
) {
}