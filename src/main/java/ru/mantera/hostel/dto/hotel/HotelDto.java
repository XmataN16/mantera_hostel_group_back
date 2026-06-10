package ru.mantera.hostel.dto.hotel;

public record HotelDto(
        Long id,
        String name,
        String shortName,
        String address,
        String phone,
        String email,
        String timezone,
        String status
) {}