package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.guest.*;
import ru.mantera.hostel.service.GuestService;

import java.util.List;

@RestController
@RequestMapping("/api/guests")
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    @GetMapping
    public List<GuestResponse> getAll() {
        return guestService.getAll();
    }

    @GetMapping("/{id}")
    public GuestResponse getById(
            @PathVariable Long id) {

        return guestService.getById(id);
    }

    @PostMapping
    public GuestResponse create(
            @RequestBody
            @Valid
            GuestCreateRequest request) {

        return guestService.create(request);
    }

    @PutMapping("/{id}")
    public GuestResponse update(
            @PathVariable Long id,
            @RequestBody
            @Valid
            GuestUpdateRequest request) {

        return guestService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id) {

        guestService.delete(id);
    }
}