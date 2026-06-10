package ru.mantera.hostel.dto.billing;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservationServiceItemResponse(
        Long id,
        Long reservationId,
        Long serviceId,
        String serviceName,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalPrice,
        OffsetDateTime createdAt
) {
}