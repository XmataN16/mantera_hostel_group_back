package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.ReservationServiceItem;

import java.util.List;
import java.util.Optional;

public interface ReservationServiceItemRepository extends JpaRepository<ReservationServiceItem, Long> {

    List<ReservationServiceItem> findByReservation_IdOrderByIdAsc(Long reservationId);

    Optional<ReservationServiceItem> findByIdAndReservation_Id(
            Long id,
            Long reservationId
    );
}