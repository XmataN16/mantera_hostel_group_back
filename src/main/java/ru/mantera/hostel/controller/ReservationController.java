package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import ru.mantera.hostel.dto.availability.AvailabilityRoomResponse;
import ru.mantera.hostel.dto.availability.BookingBoardCellResponse;
import ru.mantera.hostel.dto.reservation.ReservationCreateRequest;
import ru.mantera.hostel.dto.reservation.ReservationResponse;
import ru.mantera.hostel.dto.reservation.ReservationUpdateRequest;
import ru.mantera.hostel.service.ReservationService;

import java.time.LocalDate;
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

    @PatchMapping("/{id}/confirm")
    public ReservationResponse confirm(@PathVariable Long id) {
        return reservationService.confirm(id);
    }

    @PatchMapping("/{id}/check-in")
    public ReservationResponse checkIn(@PathVariable Long id) {
        return reservationService.checkIn(id);
    }

    @PatchMapping("/{id}/check-out")
    public ReservationResponse checkOut(@PathVariable Long id) {
        return reservationService.checkOut(id);
    }

    @PatchMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }

    @GetMapping("/availability")
    public List<AvailabilityRoomResponse> getAvailableRooms(
            @RequestParam Long hotelId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkInDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOutDate
    ) {
        return reservationService.findAvailableRooms(hotelId, checkInDate, checkOutDate);
    }

    @GetMapping("/booking-board")
    public List<BookingBoardCellResponse> getBookingBoard(
            @RequestParam Long hotelId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate
    ) {
        return reservationService.getBookingBoard(hotelId, fromDate, toDate);
    }
}