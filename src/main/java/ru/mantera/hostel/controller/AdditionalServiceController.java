package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.additionalservice.AdditionalServiceCreateRequest;
import ru.mantera.hostel.dto.additionalservice.AdditionalServiceResponse;
import ru.mantera.hostel.dto.additionalservice.AdditionalServiceUpdateRequest;
import ru.mantera.hostel.enums.ServiceStatus;
import ru.mantera.hostel.service.AdditionalServiceService;

import java.util.List;

@RestController
@RequestMapping("/api/additional-services")
@RequiredArgsConstructor
public class AdditionalServiceController {

    private final AdditionalServiceService additionalServiceService;

    @GetMapping
    public List<AdditionalServiceResponse> getAll(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) ServiceStatus status
    ) {
        return additionalServiceService.findAll(hotelId, status);
    }

    @GetMapping("/{id}")
    public AdditionalServiceResponse getById(@PathVariable Long id) {
        return additionalServiceService.findById(id);
    }

    @PostMapping
    public AdditionalServiceResponse create(
            @RequestBody @Valid AdditionalServiceCreateRequest request
    ) {
        return additionalServiceService.create(request);
    }

    @PutMapping("/{id}")
    public AdditionalServiceResponse update(
            @PathVariable Long id,
            @RequestBody @Valid AdditionalServiceUpdateRequest request
    ) {
        return additionalServiceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        additionalServiceService.delete(id);
    }
}