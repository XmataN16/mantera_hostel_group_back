package ru.mantera.hostel.dto.hotel;

import jakarta.validation.constraints.NotBlank;

public record HotelCreateRequest(

        @NotBlank
        String name,

        String shortName,

        @NotBlank
        String address,

        String phone,

        String email,

        String timezone
) {}