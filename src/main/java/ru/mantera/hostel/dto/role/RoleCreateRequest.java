// src/main/java/ru/mantera/hostel/dto/role/RoleCreateRequest.java
package ru.mantera.hostel.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleCreateRequest(
        @NotBlank(message = "Код роли не должен быть пустым")
        @Size(max = 50, message = "Код роли должен быть не длиннее 50 символов")
        String code,

        @NotBlank(message = "Название роли не должно быть пустым")
        @Size(max = 120, message = "Название роли должно быть не длиннее 120 символов")
        String name
) {}