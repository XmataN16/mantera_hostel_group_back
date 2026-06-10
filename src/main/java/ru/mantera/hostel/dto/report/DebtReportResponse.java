package ru.mantera.hostel.dto.report;

import java.math.BigDecimal;
import java.util.List;

public record DebtReportResponse(
        Long hotelId,
        BigDecimal totalDebt,
        List<DebtReportItemResponse> items
) {
}