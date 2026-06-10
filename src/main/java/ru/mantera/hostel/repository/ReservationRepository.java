package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.mantera.hostel.entity.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByReservationNumber(String reservationNumber);

    @EntityGraph(attributePaths = "reservationRooms")
    Optional<Reservation> findWithReservationRoomsById(Long id);

    @EntityGraph(attributePaths = "reservationRooms")
    List<Reservation> findAllByOrderByCreatedAtDesc();

    @Query("""
           select count(r) > 0
           from Reservation r
           where r.reservationNumber = :reservationNumber
           """)
    boolean existsDuplicateNumber(String reservationNumber);
}