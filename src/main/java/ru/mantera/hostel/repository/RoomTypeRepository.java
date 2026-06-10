package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.RoomType;

import java.util.List;

public interface RoomTypeRepository
        extends JpaRepository<RoomType, Long> {

    List<RoomType> findByHotelId(Long hotelId);
}