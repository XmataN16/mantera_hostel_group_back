package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.rate.RoomTypeRateCreateRequest;
import ru.mantera.hostel.dto.rate.RoomTypeRateResponse;
import ru.mantera.hostel.dto.rate.RoomTypeRateUpdateRequest;
import ru.mantera.hostel.service.RoomTypeRateService;

import java.util.List;

@RestController
@RequestMapping("/api/room-type-rates")
@RequiredArgsConstructor
public class RoomTypeRateController {

    private final RoomTypeRateService roomTypeRateService;

    @GetMapping
    public List<RoomTypeRateResponse> getAll(
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Long ratePlanId
    ) {
        return roomTypeRateService.findAll(roomTypeId, ratePlanId);
    }

    @GetMapping("/{id}")
    public RoomTypeRateResponse getById(@PathVariable Long id) {
        return roomTypeRateService.findById(id);
    }

    @PostMapping
    public RoomTypeRateResponse create(@RequestBody @Valid RoomTypeRateCreateRequest request) {
        return roomTypeRateService.create(request);
    }

    @PutMapping("/{id}")
    public RoomTypeRateResponse update(
            @PathVariable Long id,
            @RequestBody @Valid RoomTypeRateUpdateRequest request
    ) {
        return roomTypeRateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        roomTypeRateService.delete(id);
    }
}