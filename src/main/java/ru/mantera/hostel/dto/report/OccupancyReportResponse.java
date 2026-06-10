package ru.mantera.hostel.dto.report;

import java.time.LocalDate;

public record OccupancyReportResponse(
        Long hotelId,
        LocalDate date,
        long totalRooms,
        long occupiedRooms,
        long bookedRooms,
        long availableRooms,
        long maintenanceRooms,
        double occupancyPercent
) {
}