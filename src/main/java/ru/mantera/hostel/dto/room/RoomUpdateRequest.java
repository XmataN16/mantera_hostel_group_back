package ru.mantera.hostel.dto.room;

import jakarta.validation.constraints.Size;
import ru.mantera.hostel.enums.HousekeepingStatus;
import ru.mantera.hostel.enums.RoomStatus;

public record RoomUpdateRequest(
        Long roomTypeId,

        @Size(max = 50)
        String roomNumber,

        Integer floor,

        RoomStatus status,

        HousekeepingStatus housekeepingStatus,

        String comment
) {
}