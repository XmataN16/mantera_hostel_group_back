package ru.mantera.hostel.dto.billing;

import java.math.BigDecimal;
import java.util.List;

public record ReservationInvoiceResponse(
        Long reservationId,
        String reservationNumber,
        Long hotelId,
        Long guestId,
        BigDecimal accommodationAmount,
        BigDecimal servicesAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal debtAmount,
        List<ReservationServiceItemResponse> services,
        List<PaymentResponse> payments
) {
}