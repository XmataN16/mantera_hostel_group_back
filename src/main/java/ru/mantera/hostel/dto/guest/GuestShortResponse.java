package ru.mantera.hostel.dto.guest;

public record GuestShortResponse(
        Long id,
        String fullName,
        String phone
) {
}