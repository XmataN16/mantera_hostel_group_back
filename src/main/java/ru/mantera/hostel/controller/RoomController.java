package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.room.HousekeepingStatusUpdateRequest;
import ru.mantera.hostel.dto.room.RoomCreateRequest;
import ru.mantera.hostel.dto.room.RoomResponse;
import ru.mantera.hostel.dto.room.RoomStatusUpdateRequest;
import ru.mantera.hostel.dto.room.RoomUpdateRequest;
import ru.mantera.hostel.enums.RoomStatus;
import ru.mantera.hostel.service.RoomService;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<RoomResponse> getAll(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) RoomStatus status
    ) {
        return roomService.findAll(hotelId, status);
    }

    @GetMapping("/{id}")
    public RoomResponse getById(@PathVariable Long id) {
        return roomService.findById(id);
    }

    @PostMapping
    public RoomResponse create(@RequestBody @Valid RoomCreateRequest request) {
        return roomService.create(request);
    }

    @PutMapping("/{id}")
    public RoomResponse update(
            @PathVariable Long id,
            @RequestBody @Valid RoomUpdateRequest request
    ) {
        return roomService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public RoomResponse changeStatus(
            @PathVariable Long id,
            @RequestBody @Valid RoomStatusUpdateRequest request
    ) {
        return roomService.changeStatus(id, request);
    }

    @PatchMapping("/{id}/housekeeping-status")
    public RoomResponse changeHousekeepingStatus(
            @PathVariable Long id,
            @RequestBody @Valid HousekeepingStatusUpdateRequest request
    ) {
        return roomService.changeHousekeepingStatus(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roomService.delete(id);
    }
}