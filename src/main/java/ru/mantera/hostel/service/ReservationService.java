package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.reservation.ReservationCreateRequest;
import ru.mantera.hostel.dto.reservation.ReservationResponse;
import ru.mantera.hostel.dto.reservation.ReservationRoomRequest;
import ru.mantera.hostel.dto.reservation.ReservationRoomResponse;
import ru.mantera.hostel.dto.reservation.ReservationUpdateRequest;
import ru.mantera.hostel.entity.Reservation;
import ru.mantera.hostel.entity.ReservationRoom;
import ru.mantera.hostel.enums.ReservationRoomStatus;
import ru.mantera.hostel.enums.ReservationStatus;
import ru.mantera.hostel.exception.BadRequestException;
import ru.mantera.hostel.exception.ConflictException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.ReservationRepository;
import ru.mantera.hostel.repository.ReservationRoomRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationRoomRepository reservationRoomRepository;

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Long id) {
        Reservation reservation = reservationRepository.findWithReservationRoomsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с id=" + id + " не найдено"));
        return toResponse(reservation);
    }

    public ReservationResponse create(ReservationCreateRequest request) {
        validateDates(request.checkInDate(), request.checkOutDate());

        if (reservationRepository.existsByReservationNumber(request.reservationNumber())) {
            throw new ConflictException("Бронирование с номером '" + request.reservationNumber() + "' уже существует");
        }

        Reservation reservation = Reservation.builder()
                .hotelId(request.hotelId())
                .guestId(request.guestId())
                .reservationNumber(request.reservationNumber())
                .source(request.source())
                .status(ReservationStatus.CREATED)
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .adults(request.adults())
                .children(request.children())
                .comment(request.comment())
                .totalAmount(BigDecimal.ZERO)
                .reservationRooms(new ArrayList<>())
                .build();

        reservation = reservationRepository.save(reservation);
        attachRooms(reservation, request.rooms(), request.checkInDate(), request.checkOutDate(), null);

        reservation.setTotalAmount(calculateTotalAmount(reservation.getReservationRooms(), request.checkInDate(), request.checkOutDate()));
        reservation = reservationRepository.save(reservation);

        return toResponse(reservationRepository.findWithReservationRoomsById(reservation.getId()).orElseThrow());
    }

    public ReservationResponse update(Long id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findWithReservationRoomsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с id=" + id + " не найдено"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED || reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new BadRequestException("Нельзя изменять бронирование в статусе " + reservation.getStatus());
        }

        LocalDate newCheckIn = request.checkInDate() != null ? request.checkInDate() : reservation.getCheckInDate();
        LocalDate newCheckOut = request.checkOutDate() != null ? request.checkOutDate() : reservation.getCheckOutDate();
        validateDates(newCheckIn, newCheckOut);

        if (request.hotelId() != null) {
            reservation.setHotelId(request.hotelId());
        }
        if (request.guestId() != null) {
            reservation.setGuestId(request.guestId());
        }
        if (request.source() != null) {
            reservation.setSource(request.source());
        }
        if (request.adults() != null) {
            reservation.setAdults(request.adults());
        }
        if (request.children() != null) {
            reservation.setChildren(request.children());
        }
        if (request.comment() != null) {
            reservation.setComment(request.comment());
        }

        reservation.setCheckInDate(newCheckIn);
        reservation.setCheckOutDate(newCheckOut);

        List<ReservationRoomRequest> roomsToApply = request.rooms();
        if (roomsToApply != null) {
            reservation.getReservationRooms().clear();
            attachRooms(reservation, roomsToApply, newCheckIn, newCheckOut, reservation.getId());
        } else {
            validateExistingRoomsForPeriod(reservation, newCheckIn, newCheckOut);
        }

        reservation.setTotalAmount(calculateTotalAmount(reservation.getReservationRooms(), newCheckIn, newCheckOut));
        reservation = reservationRepository.save(reservation);

        return toResponse(reservationRepository.findWithReservationRoomsById(id).orElseThrow());
    }

    public ReservationResponse cancel(Long id) {
        Reservation reservation = reservationRepository.findWithReservationRoomsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Бронирование с id=" + id + " не найдено"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return toResponse(reservation);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        for (ReservationRoom room : reservation.getReservationRooms()) {
            room.setStatus(ReservationRoomStatus.CANCELLED);
        }

        reservation = reservationRepository.save(reservation);
        return toResponse(reservationRepository.findWithReservationRoomsById(id).orElseThrow());
    }

    private void attachRooms(
            Reservation reservation,
            List<ReservationRoomRequest> roomRequests,
            LocalDate checkIn,
            LocalDate checkOut,
            Long excludeReservationId
    ) {
        if (roomRequests == null || roomRequests.isEmpty()) {
            throw new BadRequestException("Нужно добавить хотя бы одну комнату");
        }

        for (ReservationRoomRequest roomRequest : roomRequests) {
            if (roomRequest.roomId() != null) {
                boolean overlaps = reservationRoomRepository.existsOverlapForRoomFiltered(
                        roomRequest.roomId(),
                        checkIn.toString(),
                        checkOut.toString(),
                        excludeReservationId
                );
                if (overlaps) {
                    throw new ConflictException("Комната id=" + roomRequest.roomId() + " уже занята на выбранные даты");
                }
            }

            ReservationRoom room = ReservationRoom.builder()
                    .reservation(reservation)
                    .roomTypeId(roomRequest.roomTypeId())
                    .roomId(roomRequest.roomId())
                    .ratePlanId(roomRequest.ratePlanId())
                    .guestsCount(roomRequest.guestsCount())
                    .pricePerNight(roomRequest.pricePerNight())
                    .status(ReservationRoomStatus.RESERVED)
                    .build();

            reservation.getReservationRooms().add(room);
        }
    }

    private void validateExistingRoomsForPeriod(Reservation reservation, LocalDate checkIn, LocalDate checkOut) {
        for (ReservationRoom room : reservation.getReservationRooms()) {
            if (room.getRoomId() == null) {
                continue;
            }

            boolean overlaps = reservationRoomRepository.existsOverlapForRoomFiltered(
                    room.getRoomId(),
                    checkIn.toString(),
                    checkOut.toString(),
                    reservation.getId()
            );

            if (overlaps) {
                throw new ConflictException("Комната id=" + room.getRoomId() + " уже занята на выбранные даты");
            }
        }
    }

    private BigDecimal calculateTotalAmount(List<ReservationRoom> rooms, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            throw new BadRequestException("Дата выезда должна быть позже даты заезда");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ReservationRoom room : rooms) {
            total = total.add(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
        }
        return total;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BadRequestException("checkInDate и checkOutDate обязательны");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Дата выезда должна быть позже даты заезда");
        }
    }

    private ReservationResponse toResponse(Reservation reservation) {
        List<ReservationRoomResponse> roomResponses = reservation.getReservationRooms()
                .stream()
                .map(room -> new ReservationRoomResponse(
                        room.getId(),
                        room.getRoomTypeId(),
                        room.getRoomId(),
                        room.getRatePlanId(),
                        room.getGuestsCount(),
                        room.getPricePerNight(),
                        room.getStatus()
                ))
                .toList();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getHotelId(),
                reservation.getGuestId(),
                reservation.getReservationNumber(),
                reservation.getSource(),
                reservation.getStatus(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getAdults(),
                reservation.getChildren(),
                reservation.getTotalAmount(),
                reservation.getComment(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt(),
                roomResponses
        );
    }
}