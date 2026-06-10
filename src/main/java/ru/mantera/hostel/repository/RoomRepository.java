package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.Room;
import ru.mantera.hostel.enums.RoomStatus;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByHotel_Id(Long hotelId);

    List<Room> findByHotel_IdAndStatus(Long hotelId, RoomStatus status);

    boolean existsByHotel_IdAndRoomNumberIgnoreCase(Long hotelId, String roomNumber);
}