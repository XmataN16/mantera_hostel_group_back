package ru.mantera.hostel.dto.rate;

import jakarta.validation.constraints.Size;
import ru.mantera.hostel.enums.HotelStatus;
import ru.mantera.hostel.enums.MealPlan;

public record RatePlanUpdateRequest(

        @Size(max = 50)
        String code,

        @Size(max = 120)
        String name,

        String description,

        MealPlan mealPlan,

        Boolean refundable,

        String cancellationPolicy,

        HotelStatus status
) {
}