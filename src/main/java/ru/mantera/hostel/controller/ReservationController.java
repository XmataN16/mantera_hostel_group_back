package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mantera.hostel.dto.reservation.ReservationCreateRequest;
import ru.mantera.hostel.dto.reservation.ReservationResponse;
import ru.mantera.hostel.dto.reservation.ReservationUpdateRequest;
import ru.mantera.hostel.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public List<ReservationResponse> getAll() {
        return reservationService.findAll();
    }

    @GetMapping("/{id}")
    public ReservationResponse getById(@PathVariable Long id) {
        return reservationService.findById(id);
    }

    @PostMapping
    public ReservationResponse create(@RequestBody @Valid ReservationCreateRequest request) {
        return reservationService.create(request);
    }

    @PutMapping("/{id}")
    public ReservationResponse update(
            @PathVariable Long id,
            @RequestBody @Valid ReservationUpdateRequest request
    ) {
        return reservationService.update(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }
}