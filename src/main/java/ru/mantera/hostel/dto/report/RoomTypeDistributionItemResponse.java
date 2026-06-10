package ru.mantera.hostel.dto.report;

public record RoomTypeDistributionItemResponse(
        Long roomTypeId,
        String roomTypeName,
        long staysCount
) {
}