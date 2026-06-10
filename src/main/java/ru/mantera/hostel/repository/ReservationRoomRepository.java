package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.mantera.hostel.entity.ReservationRoom;

import java.util.List;

public interface ReservationRoomRepository extends JpaRepository<ReservationRoom, Long> {

    List<ReservationRoom> findByReservationId(Long reservationId);

    @Query(value = """
            select case when count(*) > 0 then true else false end
            from reservation_rooms rr
            join reservations r on r.id = rr.reservation_id
            where rr.room_id = :roomId
              and rr.status <> 'CANCELLED'
              and r.status <> 'CANCELLED'
              and r.check_in_date < :checkOutDate
              and r.check_out_date > :checkInDate
            """, nativeQuery = true)
    boolean existsOverlapForRoom(Long roomId, String checkInDate, String checkOutDate, Long excludeReservationId);

    @Query(value = """
            select case when count(*) > 0 then true else false end
            from reservation_rooms rr
            join reservations r on r.id = rr.reservation_id
            where rr.room_id = :roomId
              and rr.status <> 'CANCELLED'
              and r.status <> 'CANCELLED'
              and r.check_in_date < :checkOutDate
              and r.check_out_date > :checkInDate
              and r.id <> coalesce(:excludeReservationId, -1)
            """, nativeQuery = true)
    boolean existsOverlapForRoomFiltered(Long roomId, String checkInDate, String checkOutDate, Long excludeReservationId);
}