package ru.mantera.hostel.dto.report;

import java.time.LocalDate;

public record CheckInOutReportResponse(
        Long hotelId,
        LocalDate date,
        long checkIns,
        long checkOuts
) {
}