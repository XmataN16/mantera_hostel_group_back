package ru.mantera.hostel.dto.billing;

import ru.mantera.hostel.enums.PaymentMethod;
import ru.mantera.hostel.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        Long reservationId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String transactionReference,
        OffsetDateTime paidAt,
        String comment,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}