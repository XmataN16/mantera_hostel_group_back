package ru.mantera.hostel.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueReportResponse(
        Long hotelId,
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal accommodationRevenue,
        BigDecimal servicesRevenue,
        BigDecimal totalRevenue
) {
}