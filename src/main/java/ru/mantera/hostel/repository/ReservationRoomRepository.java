package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.mantera.hostel.entity.ReservationRoom;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRoomRepository extends JpaRepository<ReservationRoom, Long> {

    List<ReservationRoom> findByReservationId(Long reservationId);

    @Query(value = """
            select case when count(*) > 0 then true else false end
            from reservation_rooms rr
            join reservations r on r.id = rr.reservation_id
            where rr.room_id = :roomId
              and rr.status <> 'CANCELLED'
              and r.status not in ('CANCELLED', 'NO_SHOW')
              and r.check_in_date < :checkOutDate
              and r.check_out_date > :checkInDate
              and (:excludeReservationId is null or r.id <> :excludeReservationId)
            """, nativeQuery = true)
    boolean existsOverlapForRoomFiltered(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("excludeReservationId") Long excludeReservationId
    );

    @Query(value = """
            select rr.*
            from reservation_rooms rr
            join reservations r on r.id = rr.reservation_id
            where r.hotel_id = :hotelId
              and rr.room_id is not null
              and rr.status <> 'CANCELLED'
              and r.status not in ('CANCELLED', 'NO_SHOW')
              and r.check_in_date < :toDate
              and r.check_out_date > :fromDate
            order by rr.room_id, r.check_in_date
            """, nativeQuery = true)
    List<ReservationRoom> findBookingBoardRows(
            @Param("hotelId") Long hotelId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}