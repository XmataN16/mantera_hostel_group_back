package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.rate.RatePlanCreateRequest;
import ru.mantera.hostel.dto.rate.RatePlanResponse;
import ru.mantera.hostel.dto.rate.RatePlanUpdateRequest;
import ru.mantera.hostel.service.RatePlanService;

import java.util.List;

@RestController
@RequestMapping("/api/rate-plans")
@RequiredArgsConstructor
public class RatePlanController {

    private final RatePlanService ratePlanService;

    @GetMapping
    public List<RatePlanResponse> getAll(@RequestParam(required = false) Long hotelId) {
        return ratePlanService.findAll(hotelId);
    }

    @GetMapping("/{id}")
    public RatePlanResponse getById(@PathVariable Long id) {
        return ratePlanService.findById(id);
    }

    @PostMapping
    public RatePlanResponse create(@RequestBody @Valid RatePlanCreateRequest request) {
        return ratePlanService.create(request);
    }

    @PutMapping("/{id}")
    public RatePlanResponse update(
            @PathVariable Long id,
            @RequestBody @Valid RatePlanUpdateRequest request
    ) {
        return ratePlanService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        ratePlanService.delete(id);
    }
}