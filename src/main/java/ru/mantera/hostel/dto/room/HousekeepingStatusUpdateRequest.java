package ru.mantera.hostel.dto.room;

import jakarta.validation.constraints.NotNull;
import ru.mantera.hostel.enums.HousekeepingStatus;

public record HousekeepingStatusUpdateRequest(
        @NotNull
        HousekeepingStatus housekeepingStatus
) {
}