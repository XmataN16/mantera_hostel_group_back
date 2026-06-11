package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mantera.hostel.dto.availability.AvailabilityRoomResponse;
import ru.mantera.hostel.dto.availability.BookingBoardCellResponse;
import ru.mantera.hostel.dto.reservation.ReservationCreateRequest;
import ru.mantera.hostel.dto.reservation.ReservationResponse;
import ru.mantera.hostel.dto.reservation.ReservationRoomRequest;
import ru.mantera.hostel.dto.reservation.ReservationRoomResponse;
import ru.mantera.hostel.dto.reservation.ReservationUpdateRequest;
import ru.mantera.hostel.entity.Guest;
import ru.mantera.hostel.entity.Hotel;
import ru.mantera.hostel.entity.Reservation;
import ru.mantera.hostel.entity.ReservationRoom;
import ru.mantera.hostel.entity.Room;
import ru.mantera.hostel.entity.RoomType;
import ru.mantera.hostel.enums.HousekeepingStatus;
import ru.mantera.hostel.enums.ReservationRoomStatus;
import ru.mantera.hostel.enums.ReservationStatus;
import ru.mantera.hostel.enums.RoomStatus;
import ru.mantera.hostel.exception.BadRequestException;
import ru.mantera.hostel.exception.ConflictException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.GuestRepository;
import ru.mantera.hostel.repository.HotelRepository;
import ru.mantera.hostel.repository.ReservationRepository;
import ru.mantera.hostel.repository.ReservationRoomRepository;
import ru.mantera.hostel.repository.RoomRepository;
import ru.mantera.hostel.repository.RoomTypeRepository;
import ru.mantera.hostel.entity.RatePlan;
import ru.mantera.hostel.repository.RatePlanRepository;
import ru.mantera.hostel.repository.RoomTypeRateRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private static final DateTimeFormatter RESERVATION_NUMBER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ReservationRepository reservationRepository;
    private final ReservationRoomRepository reservationRoomRepository;
    private final HotelRepository hotelRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;
    private final RoomTypeRateRepository roomTypeRateRepository;

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Long id) {
        Reservation reservation = getReservationWithRoomsOrThrow(id);
        return toResponse(reservation);
    }

    public ReservationResponse create(ReservationCreateRequest request) {
        validateDates(request.checkInDate(), request.checkOutDate());

        Hotel hotel = getHotelOrThrow(request.hotelId());
        Guest guest = getGuestOrThrow(request.guestId());

        String reservationNumber = normalizeReservationNumber(request.reservationNumber());

        if (reservationRepository.existsByReservationNumber(reservationNumber)) {
            throw new ConflictException("Бронирование с номером '" + reservationNumber + "' уже существует");
        }

        Reservation reservation = Reservation.builder()
                .hotelId(hotel.getId())
                .guestId(guest.getId())
                .reservationNumber(reservationNumber)
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

        attachRooms(
                reservation,
                request.rooms(),
                request.checkInDate(),
                request.checkOutDate(),
                null
        );

        reservation.setTotalAmount(
                calculateTotalAmount(
                        reservation.getReservationRooms(),
                        request.checkInDate(),
                        request.checkOutDate()
                )
        );

        reservation = reservationRepository.save(reservation);

        return toResponse(
                reservationRepository.findWithReservationRoomsById(reservation.getId())
                        .orElseThrow()
        );
    }

    public ReservationResponse update(Long id, ReservationUpdateRequest request) {
        Reservation reservation = getReservationWithRoomsOrThrow(id);

        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new BadRequestException("Нельзя изменять бронирование в статусе " + reservation.getStatus());
        }

        Long newHotelId = request.hotelId() != null
                ? request.hotelId()
                : reservation.getHotelId();

        Long newGuestId = request.guestId() != null
                ? request.guestId()
                : reservation.getGuestId();

        LocalDate newCheckIn = request.checkInDate() != null
                ? request.checkInDate()
                : reservation.getCheckInDate();

        LocalDate newCheckOut = request.checkOutDate() != null
                ? request.checkOutDate()
                : reservation.getCheckOutDate();

        validateDates(newCheckIn, newCheckOut);

        getHotelOrThrow(newHotelId);
        getGuestOrThrow(newGuestId);

        reservation.setHotelId(newHotelId);
        reservation.setGuestId(newGuestId);

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

        if (request.rooms() != null) {
            reservation.getReservationRooms().clear();

            attachRooms(
                    reservation,
                    request.rooms(),
                    newCheckIn,
                    newCheckOut,
                    reservation.getId()
            );
        } else {
            validateExistingRoomsForPeriod(reservation, newCheckIn, newCheckOut);
        }

        reservation.setTotalAmount(
                calculateTotalAmount(
                        reservation.getReservationRooms(),
                        newCheckIn,
                        newCheckOut
                )
        );

        reservation = reservationRepository.save(reservation);

        return toResponse(
                reservationRepository.findWithReservationRoomsById(reservation.getId())
                        .orElseThrow()
        );
    }

    public ReservationResponse confirm(Long id) {
        Reservation reservation = getReservationWithRoomsOrThrow(id);

        if (reservation.getStatus() != ReservationStatus.CREATED) {
            throw new BadRequestException("Подтвердить можно только бронирование в статусе CREATED");
        }

        validateExistingRoomsForPeriod(
                reservation,
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        reservation.setStatus(ReservationStatus.CONFIRMED);

        return toResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse checkIn(Long id) {
        Reservation reservation = getReservationWithRoomsOrThrow(id);

        if (reservation.getStatus() != ReservationStatus.CREATED
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("Заселить можно только бронь в статусе CREATED или CONFIRMED");
        }

        if (reservation.getReservationRooms().isEmpty()) {
            throw new BadRequestException("В бронировании нет номеров для заселения");
        }

        for (ReservationRoom reservationRoom : reservation.getReservationRooms()) {
            if (reservationRoom.getRoomId() == null) {
                throw new BadRequestException("Для заселения все позиции бронирования должны иметь конкретный roomId");
            }

            Room room = getRoomOrThrow(reservationRoom.getRoomId());

            if (room.getStatus() == RoomStatus.MAINTENANCE
                    || room.getStatus() == RoomStatus.OUT_OF_SERVICE) {
                throw new BadRequestException("Номер " + room.getRoomNumber() + " недоступен для заселения");
            }

            if (room.getHousekeepingStatus() == HousekeepingStatus.DIRTY) {
                throw new BadRequestException("Номер " + room.getRoomNumber() + " требует уборки перед заселением");
            }

            reservationRoom.setStatus(ReservationRoomStatus.CHECKED_IN);
            room.setStatus(RoomStatus.OCCUPIED);
            roomRepository.save(room);
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);

        return toResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse checkOut(Long id) {
        Reservation reservation = getReservationWithRoomsOrThrow(id);

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BadRequestException("Выселить можно только бронирование в статусе CHECKED_IN");
        }

        for (ReservationRoom reservationRoom : reservation.getReservationRooms()) {
            reservationRoom.setStatus(ReservationRoomStatus.CHECKED_OUT);

            if (reservationRoom.getRoomId() != null) {
                Room room = getRoomOrThrow(reservationRoom.getRoomId());
                room.setStatus(RoomStatus.AVAILABLE);
                room.setHousekeepingStatus(HousekeepingStatus.DIRTY);
                roomRepository.save(room);
            }
        }

        reservation.setStatus(ReservationStatus.CHECKED_OUT);

        return toResponse(reservationRepository.save(reservation));
    }

    public ReservationResponse cancel(Long id) {
        Reservation reservation = getReservationWithRoomsOrThrow(id);

        if (reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new BadRequestException("Нельзя отменить уже завершённое проживание");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return toResponse(reservation);
        }

        for (ReservationRoom reservationRoom : reservation.getReservationRooms()) {
            reservationRoom.setStatus(ReservationRoomStatus.CANCELLED);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<AvailabilityRoomResponse> findAvailableRooms(
            Long hotelId,
            LocalDate checkInDate,
            LocalDate checkOutDate
    ) {
        validateDates(checkInDate, checkOutDate);
        getHotelOrThrow(hotelId);

        return roomRepository.findByHotel_Id(hotelId)
                .stream()
                .filter(room -> room.getStatus() != RoomStatus.MAINTENANCE)
                .filter(room -> room.getStatus() != RoomStatus.OUT_OF_SERVICE)
                .filter(room -> !reservationRoomRepository.existsOverlapForRoomFiltered(
                        room.getId(),
                        checkInDate,
                        checkOutDate,
                        null
                ))
                .map(this::toAvailabilityResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingBoardCellResponse> getBookingBoard(
            Long hotelId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        validateDates(fromDate, toDate);
        getHotelOrThrow(hotelId);

        List<Room> rooms = roomRepository.findByHotel_Id(hotelId);
        List<ReservationRoom> reservationRooms =
                reservationRoomRepository.findBookingBoardRows(hotelId, fromDate, toDate);

        List<BookingBoardCellResponse> result = new ArrayList<>();

        for (Room room : rooms) {
            LocalDate currentDate = fromDate;

            while (currentDate.isBefore(toDate)) {
                ReservationRoom matchedReservationRoom = findReservationForRoomAndDate(
                        reservationRooms,
                        room.getId(),
                        currentDate
                );

                if (matchedReservationRoom == null) {
                    result.add(new BookingBoardCellResponse(
                            room.getId(),
                            room.getRoomNumber(),
                            currentDate,
                            room.getStatus().name(),
                            null,
                            null,
                            null
                    ));
                } else {
                    Reservation reservation = matchedReservationRoom.getReservation();

                    result.add(new BookingBoardCellResponse(
                            room.getId(),
                            room.getRoomNumber(),
                            currentDate,
                            reservation.getStatus().name(),
                            reservation.getId(),
                            reservation.getReservationNumber(),
                            reservation.getGuestId()
                    ));
                }

                currentDate = currentDate.plusDays(1);
            }
        }

        return result;
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

        Set<Long> roomIdsInRequest = new HashSet<>();

        for (ReservationRoomRequest roomRequest : roomRequests) {
            RoomType roomType = getRoomTypeOrThrow(roomRequest.roomTypeId());

            if (!roomType.getHotel().getId().equals(reservation.getHotelId())) {
                throw new BadRequestException("Категория номера id=" + roomType.getId()
                        + " не относится к выбранному отелю");
            }

            if (roomRequest.guestsCount() > roomType.getCapacity()) {
                throw new BadRequestException("Количество гостей превышает вместимость категории номера "
                        + roomType.getName());
            }

            if (roomRequest.roomId() != null) {
                if (!roomIdsInRequest.add(roomRequest.roomId())) {
                    throw new BadRequestException("Один и тот же номер нельзя добавить в бронирование дважды");
                }

                Room room = getRoomOrThrow(roomRequest.roomId());

                if (!room.getHotel().getId().equals(reservation.getHotelId())) {
                    throw new BadRequestException("Номер id=" + room.getId() + " не относится к выбранному отелю");
                }

                if (!room.getRoomType().getId().equals(roomType.getId())) {
                    throw new BadRequestException("Номер " + room.getRoomNumber()
                            + " не относится к выбранной категории номера");
                }

                if (room.getStatus() == RoomStatus.MAINTENANCE
                        || room.getStatus() == RoomStatus.OUT_OF_SERVICE) {
                    throw new BadRequestException("Номер " + room.getRoomNumber()
                            + " недоступен для бронирования");
                }

                boolean overlaps = reservationRoomRepository.existsOverlapForRoomFiltered(
                        roomRequest.roomId(),
                        checkIn,
                        checkOut,
                        excludeReservationId
                );

                if (overlaps) {
                    throw new ConflictException("Номер id=" + roomRequest.roomId()
                            + " уже занят на выбранные даты");
                }
            }

            BigDecimal pricePerNight = resolvePricePerNight(
                    roomRequest,
                    roomType,
                    checkIn
            );

            ReservationRoom reservationRoom = ReservationRoom.builder()
                    .reservation(reservation)
                    .roomTypeId(roomType.getId())
                    .roomId(roomRequest.roomId())
                    .ratePlanId(roomRequest.ratePlanId())
                    .guestsCount(roomRequest.guestsCount())
                    .pricePerNight(pricePerNight)
                    .status(ReservationRoomStatus.RESERVED)
                    .build();

            reservation.getReservationRooms().add(reservationRoom);
        }
    }

    private BigDecimal resolvePricePerNight(
            ReservationRoomRequest roomRequest,
            RoomType roomType,
            LocalDate checkIn
    ) {
        if (roomRequest.pricePerNight() != null) {
            return roomRequest.pricePerNight();
        }

        if (roomRequest.ratePlanId() == null) {
            return roomType.getBasePrice();
        }

        RatePlan ratePlan = ratePlanRepository.findById(roomRequest.ratePlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Тариф с id=" + roomRequest.ratePlanId() + " не найден"
                ));

        if (!ratePlan.getHotel().getId().equals(roomType.getHotel().getId())) {
            throw new BadRequestException("Тариф не относится к отелю выбранной категории номера");
        }

        return roomTypeRateRepository.findActualRate(
                        roomType.getId(),
                        ratePlan.getId(),
                        checkIn
                )
                .map(rate -> rate.getPrice())
                .orElse(roomType.getBasePrice());
    }

    private void validateExistingRoomsForPeriod(
            Reservation reservation,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        for (ReservationRoom reservationRoom : reservation.getReservationRooms()) {
            if (reservationRoom.getRoomId() == null) {
                continue;
            }

            boolean overlaps = reservationRoomRepository.existsOverlapForRoomFiltered(
                    reservationRoom.getRoomId(),
                    checkIn,
                    checkOut,
                    reservation.getId()
            );

            if (overlaps) {
                throw new ConflictException("Номер id=" + reservationRoom.getRoomId()
                        + " уже занят на выбранные даты");
            }
        }
    }

    private BigDecimal calculateTotalAmount(
            List<ReservationRoom> rooms,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        if (nights <= 0) {
            throw new BadRequestException("Дата выезда должна быть позже даты заезда");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (ReservationRoom room : rooms) {
            total = total.add(
                    room.getPricePerNight().multiply(BigDecimal.valueOf(nights))
            );
        }

        return total;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BadRequestException("Дата заезда и дата выезда обязательны");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Дата выезда должна быть позже даты заезда");
        }
    }

    private String normalizeReservationNumber(String reservationNumber) {
        if (reservationNumber != null && !reservationNumber.isBlank()) {
            return reservationNumber.trim();
        }

        for (int i = 0; i < 10; i++) {
            String generated = "RES-"
                    + LocalDate.now().format(RESERVATION_NUMBER_DATE_FORMAT)
                    + "-"
                    + (System.currentTimeMillis() % 100000)
                    + i;

            if (!reservationRepository.existsByReservationNumber(generated)) {
                return generated;
            }
        }

        throw new ConflictException("Не удалось сформировать уникальный номер бронирования");
    }

    private ReservationRoom findReservationForRoomAndDate(
            List<ReservationRoom> reservationRooms,
            Long roomId,
            LocalDate date
    ) {
        for (ReservationRoom reservationRoom : reservationRooms) {
            if (!roomId.equals(reservationRoom.getRoomId())) {
                continue;
            }

            Reservation reservation = reservationRoom.getReservation();

            boolean inRange = !date.isBefore(reservation.getCheckInDate())
                    && date.isBefore(reservation.getCheckOutDate());

            if (inRange) {
                return reservationRoom;
            }
        }

        return null;
    }

    private Reservation getReservationWithRoomsOrThrow(Long id) {
        return reservationRepository.findWithReservationRoomsById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Бронирование с id=" + id + " не найдено"));
    }

    private Hotel getHotelOrThrow(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Отель с id=" + id + " не найден"));
    }

    private Guest getGuestOrThrow(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Гость с id=" + id + " не найден"));
    }

    private Room getRoomOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Номер с id=" + id + " не найден"));
    }

    private RoomType getRoomTypeOrThrow(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Категория номера с id=" + id + " не найдена"));
    }

    private AvailabilityRoomResponse toAvailabilityResponse(Room room) {
        return new AvailabilityRoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getFloor(),
                room.getHotel().getId(),
                room.getHotel().getName(),
                room.getRoomType().getId(),
                room.getRoomType().getName(),
                room.getRoomType().getCapacity(),
                room.getRoomType().getBasePrice(),
                room.getStatus(),
                room.getHousekeepingStatus()
        );
    }

    private ReservationResponse toResponse(Reservation reservation) {
        List<ReservationRoomResponse> roomResponses =
                reservation.getReservationRooms()
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