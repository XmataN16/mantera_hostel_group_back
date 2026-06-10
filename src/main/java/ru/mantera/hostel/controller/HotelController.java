package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import ru.mantera.hostel.dto.hotel.HotelCreateRequest;
import ru.mantera.hostel.dto.hotel.HotelDto;
import ru.mantera.hostel.service.HotelService;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public List<HotelDto> getAll() {
        return hotelService.getAll();
    }

    @PostMapping
    public HotelDto create(
            @RequestBody @Valid HotelCreateRequest request
    ) {
        return hotelService.create(request);
    }
}