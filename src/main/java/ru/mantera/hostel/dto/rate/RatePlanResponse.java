package ru.mantera.hostel.dto.rate;

import ru.mantera.hostel.enums.HotelStatus;
import ru.mantera.hostel.enums.MealPlan;

import java.time.OffsetDateTime;

public record RatePlanResponse(
        Long id,
        Long hotelId,
        String hotelName,
        String code,
        String name,
        String description,
        MealPlan mealPlan,
        Boolean refundable,
        String cancellationPolicy,
        HotelStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}