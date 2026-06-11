package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.employee.EmployeeCreateRequest;
import ru.mantera.hostel.dto.employee.EmployeeResponse;
import ru.mantera.hostel.dto.employee.EmployeeUpdateRequest;
import ru.mantera.hostel.enums.EmployeeStatus;
import ru.mantera.hostel.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public List<EmployeeResponse> getAll(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) EmployeeStatus status
    ) {
        return employeeService.findAll(hotelId, status);
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @PostMapping
    public EmployeeResponse create(@RequestBody @Valid EmployeeCreateRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(
            @PathVariable Long id,
            @RequestBody @Valid EmployeeUpdateRequest request
    ) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        employeeService.delete(id);
    }
}