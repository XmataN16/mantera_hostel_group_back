package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.room.HousekeepingStatusUpdateRequest;
import ru.mantera.hostel.dto.room.RoomCreateRequest;
import ru.mantera.hostel.dto.room.RoomResponse;
import ru.mantera.hostel.dto.room.RoomStatusUpdateRequest;
import ru.mantera.hostel.dto.room.RoomUpdateRequest;
import ru.mantera.hostel.entity.Hotel;
import ru.mantera.hostel.entity.Room;
import ru.mantera.hostel.entity.RoomType;
import ru.mantera.hostel.enums.HousekeepingStatus;
import ru.mantera.hostel.enums.RoomStatus;
import ru.mantera.hostel.exception.BadRequestException;
import ru.mantera.hostel.exception.ConflictException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.HotelRepository;
import ru.mantera.hostel.repository.RoomRepository;
import ru.mantera.hostel.repository.RoomTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Transactional(readOnly = true)
    public List<RoomResponse> findAll(Long hotelId, RoomStatus status) {
        List<Room> rooms;

        if (hotelId != null && status != null) {
            rooms = roomRepository.findByHotel_IdAndStatus(hotelId, status);
        } else if (hotelId != null) {
            rooms = roomRepository.findByHotel_Id(hotelId);
        } else {
            rooms = roomRepository.findAll();
        }

        return rooms.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse findById(Long id) {
        return toResponse(getRoomOrThrow(id));
    }

    public RoomResponse create(RoomCreateRequest request) {
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Отель с id=" + request.hotelId() + " не найден"));

        RoomType roomType = roomTypeRepository.findById(request.roomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Категория номера с id=" + request.roomTypeId() + " не найдена"));

        if (!roomType.getHotel().getId().equals(hotel.getId())) {
            throw new BadRequestException("Категория номера не относится к выбранному отелю");
        }

        if (roomRepository.existsByHotel_IdAndRoomNumberIgnoreCase(request.hotelId(), request.roomNumber())) {
            throw new ConflictException("Номер '" + request.roomNumber() + "' уже существует в этом отеле");
        }

        Room room = Room.builder()
                .hotel(hotel)
                .roomType(roomType)
                .roomNumber(request.roomNumber())
                .floor(request.floor())
                .status(RoomStatus.AVAILABLE)
                .housekeepingStatus(HousekeepingStatus.CLEAN)
                .comment(request.comment())
                .build();

        return toResponse(roomRepository.save(room));
    }

    public RoomResponse update(Long id, RoomUpdateRequest request) {
        Room room = getRoomOrThrow(id);

        if (request.roomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(request.roomTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Категория номера с id=" + request.roomTypeId() + " не найдена"));

            if (!roomType.getHotel().getId().equals(room.getHotel().getId())) {
                throw new BadRequestException("Категория номера не относится к отелю текущего номера");
            }

            room.setRoomType(roomType);
        }

        if (request.roomNumber() != null && !request.roomNumber().equalsIgnoreCase(room.getRoomNumber())) {
            if (roomRepository.existsByHotel_IdAndRoomNumberIgnoreCase(room.getHotel().getId(), request.roomNumber())) {
                throw new ConflictException("Номер '" + request.roomNumber() + "' уже существует в этом отеле");
            }

            room.setRoomNumber(request.roomNumber());
        }

        if (request.floor() != null) {
            room.setFloor(request.floor());
        }

        if (request.status() != null) {
            room.setStatus(request.status());
        }

        if (request.housekeepingStatus() != null) {
            room.setHousekeepingStatus(request.housekeepingStatus());
        }

        if (request.comment() != null) {
            room.setComment(request.comment());
        }

        return toResponse(roomRepository.save(room));
    }

    public RoomResponse changeStatus(Long id, RoomStatusUpdateRequest request) {
        Room room = getRoomOrThrow(id);
        room.setStatus(request.status());
        return toResponse(roomRepository.save(room));
    }

    public RoomResponse changeHousekeepingStatus(Long id, HousekeepingStatusUpdateRequest request) {
        Room room = getRoomOrThrow(id);
        room.setHousekeepingStatus(request.housekeepingStatus());
        return toResponse(roomRepository.save(room));
    }

    public void delete(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Номер с id=" + id + " не найден");
        }

        roomRepository.deleteById(id);
    }

    private Room getRoomOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Номер с id=" + id + " не найден"));
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHotel().getId(),
                room.getHotel().getName(),
                room.getRoomType().getId(),
                room.getRoomType().getName(),
                room.getRoomNumber(),
                room.getFloor(),
                room.getStatus(),
                room.getHousekeepingStatus(),
                room.getComment(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}