package ru.mantera.hostel.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebtReportItemResponse(
        Long reservationId,
        String reservationNumber,
        Long guestId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal debtAmount
) {
}