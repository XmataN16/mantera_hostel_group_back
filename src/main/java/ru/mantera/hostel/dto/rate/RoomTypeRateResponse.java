package ru.mantera.hostel.dto.rate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record RoomTypeRateResponse(
        Long id,
        Long roomTypeId,
        String roomTypeName,
        Long ratePlanId,
        String ratePlanName,
        LocalDate validFrom,
        LocalDate validTo,
        BigDecimal price,
        OffsetDateTime createdAt
) {
}