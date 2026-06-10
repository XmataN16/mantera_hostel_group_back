// src/main/java/ru/mantera/hostel/dto/role/RoleDto.java
package ru.mantera.hostel.dto.role;

public record RoleDto(
        Long id,
        String code,
        String name
) {}