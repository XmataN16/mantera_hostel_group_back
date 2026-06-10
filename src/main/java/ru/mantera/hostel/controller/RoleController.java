// src/main/java/ru/mantera/hostel/controller/RoleController.java
package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mantera.hostel.dto.role.RoleCreateRequest;
import ru.mantera.hostel.dto.role.RoleDto;
import ru.mantera.hostel.service.RoleService;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<RoleDto> getAllRoles() {
        return roleService.findAll();
    }

    @PostMapping
    public RoleDto createRole(@RequestBody @Valid RoleCreateRequest request) {
        return roleService.create(request);
    }
}