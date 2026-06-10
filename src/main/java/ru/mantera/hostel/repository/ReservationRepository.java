package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import ru.mantera.hostel.entity.Reservation;
import ru.mantera.hostel.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByReservationNumber(String reservationNumber);

    @EntityGraph(attributePaths = "reservationRooms")
    Optional<Reservation> findWithReservationRoomsById(Long id);

    @EntityGraph(attributePaths = "reservationRooms")
    List<Reservation> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "reservationRooms")
    List<Reservation> findByHotelIdAndCheckInDateLessThanAndCheckOutDateGreaterThanOrderByCheckInDateAsc(
            Long hotelId,
            LocalDate toDate,
            LocalDate fromDate
    );

    List<Reservation> findByHotelIdAndStatus(Long hotelId, ReservationStatus status);

    List<Reservation> findByGuestIdOrderByCheckInDateDesc(Long guestId);
}