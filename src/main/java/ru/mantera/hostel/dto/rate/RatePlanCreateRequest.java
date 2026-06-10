package ru.mantera.hostel.dto.rate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.mantera.hostel.enums.MealPlan;

public record RatePlanCreateRequest(

        @NotNull(message = "hotelId обязателен")
        Long hotelId,

        @NotBlank(message = "code обязателен")
        @Size(max = 50)
        String code,

        @NotBlank(message = "name обязателен")
        @Size(max = 120)
        String name,

        String description,

        @NotNull(message = "mealPlan обязателен")
        MealPlan mealPlan,

        @NotNull(message = "refundable обязателен")
        Boolean refundable,

        String cancellationPolicy
) {
}