// src/main/java/ru/mantera/hostel/mapper/RoleMapper.java
package ru.mantera.hostel.mapper;

import ru.mantera.hostel.dto.role.RoleCreateRequest;
import ru.mantera.hostel.dto.role.RoleDto;
import ru.mantera.hostel.entity.Role;

public final class RoleMapper {

    private RoleMapper() {
    }

    public static RoleDto toDto(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleDto(role.getId(), role.getCode(), role.getName());
    }

    public static Role toEntity(RoleCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Role.builder()
                .code(request.code())
                .name(request.name())
                .build();
    }
}