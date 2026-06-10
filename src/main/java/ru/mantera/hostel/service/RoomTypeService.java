package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.roomtype.RoomTypeCreateRequest;
import ru.mantera.hostel.dto.roomtype.RoomTypeResponse;
import ru.mantera.hostel.dto.roomtype.RoomTypeUpdateRequest;
import ru.mantera.hostel.entity.Hotel;
import ru.mantera.hostel.entity.RoomType;
import ru.mantera.hostel.enums.HotelStatus;
import ru.mantera.hostel.exception.ConflictException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.HotelRepository;
import ru.mantera.hostel.repository.RoomTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    public List<RoomTypeResponse> findAll(Long hotelId) {
        List<RoomType> roomTypes = hotelId == null
                ? roomTypeRepository.findAll()
                : roomTypeRepository.findByHotel_Id(hotelId);

        return roomTypes.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomTypeResponse findById(Long id) {
        RoomType roomType = getRoomTypeOrThrow(id);
        return toResponse(roomType);
    }

    public RoomTypeResponse create(RoomTypeCreateRequest request) {
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Отель с id=" + request.hotelId() + " не найден"));

        if (roomTypeRepository.existsByHotel_IdAndCodeIgnoreCase(request.hotelId(), request.code())) {
            throw new ConflictException("Категория номера с кодом '" + request.code() + "' уже существует в этом отеле");
        }

        RoomType roomType = RoomType.builder()
                .hotel(hotel)
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .capacity(request.capacity())
                .basePrice(request.basePrice())
                .status(HotelStatus.ACTIVE)
                .build();

        return toResponse(roomTypeRepository.save(roomType));
    }

    public RoomTypeResponse update(Long id, RoomTypeUpdateRequest request) {
        RoomType roomType = getRoomTypeOrThrow(id);

        if (request.code() != null && !request.code().equalsIgnoreCase(roomType.getCode())) {
            boolean exists = roomTypeRepository.existsByHotel_IdAndCodeIgnoreCase(
                    roomType.getHotel().getId(),
                    request.code()
            );

            if (exists) {
                throw new ConflictException("Категория номера с кодом '" + request.code() + "' уже существует в этом отеле");
            }

            roomType.setCode(request.code());
        }

        if (request.name() != null) {
            roomType.setName(request.name());
        }

        if (request.description() != null) {
            roomType.setDescription(request.description());
        }

        if (request.capacity() != null) {
            roomType.setCapacity(request.capacity());
        }

        if (request.basePrice() != null) {
            roomType.setBasePrice(request.basePrice());
        }

        if (request.status() != null) {
            roomType.setStatus(request.status());
        }

        return toResponse(roomTypeRepository.save(roomType));
    }

    public void delete(Long id) {
        if (!roomTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Категория номера с id=" + id + " не найдена");
        }

        roomTypeRepository.deleteById(id);
    }

    private RoomType getRoomTypeOrThrow(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категория номера с id=" + id + " не найдена"));
    }

    private RoomTypeResponse toResponse(RoomType roomType) {
        return new RoomTypeResponse(
                roomType.getId(),
                roomType.getHotel().getId(),
                roomType.getHotel().getName(),
                roomType.getCode(),
                roomType.getName(),
                roomType.getDescription(),
                roomType.getCapacity(),
                roomType.getBasePrice(),
                roomType.getStatus(),
                roomType.getCreatedAt(),
                roomType.getUpdatedAt()
        );
    }
}