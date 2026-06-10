package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.hotel.HotelCreateRequest;
import ru.mantera.hostel.dto.hotel.HotelDto;
import ru.mantera.hostel.entity.Hotel;
import ru.mantera.hostel.repository.HotelRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelService {

    private final HotelRepository hotelRepository;

    public List<HotelDto> getAll() {
        return hotelRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public HotelDto create(HotelCreateRequest request) {

        Hotel hotel = Hotel.builder()
                .name(request.name())
                .shortName(request.shortName())
                .address(request.address())
                .phone(request.phone())
                .email(request.email())
                .timezone(request.timezone())
                .status("ACTIVE")
                .build();

        return toDto(
                hotelRepository.save(hotel)
        );
    }

    private HotelDto toDto(Hotel hotel) {
        return new HotelDto(
                hotel.getId(),
                hotel.getName(),
                hotel.getShortName(),
                hotel.getAddress(),
                hotel.getPhone(),
                hotel.getEmail(),
                hotel.getTimezone(),
                hotel.getStatus()
        );
    }
}