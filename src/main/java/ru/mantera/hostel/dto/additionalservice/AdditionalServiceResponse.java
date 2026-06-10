package ru.mantera.hostel.dto.additionalservice;

import ru.mantera.hostel.enums.ServiceStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AdditionalServiceResponse(
        Long id,
        Long hotelId,
        String hotelName,
        String name,
        String description,
        BigDecimal price,
        ServiceStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}