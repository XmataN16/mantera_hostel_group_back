package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.roomtype.RoomTypeCreateRequest;
import ru.mantera.hostel.dto.roomtype.RoomTypeResponse;
import ru.mantera.hostel.dto.roomtype.RoomTypeUpdateRequest;
import ru.mantera.hostel.service.RoomTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
@RequiredArgsConstructor
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    @GetMapping
    public List<RoomTypeResponse> getAll(@RequestParam(required = false) Long hotelId) {
        return roomTypeService.findAll(hotelId);
    }

    @GetMapping("/{id}")
    public RoomTypeResponse getById(@PathVariable Long id) {
        return roomTypeService.findById(id);
    }

    @PostMapping
    public RoomTypeResponse create(@RequestBody @Valid RoomTypeCreateRequest request) {
        return roomTypeService.create(request);
    }

    @PutMapping("/{id}")
    public RoomTypeResponse update(
            @PathVariable Long id,
            @RequestBody @Valid RoomTypeUpdateRequest request
    ) {
        return roomTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roomTypeService.delete(id);
    }
}