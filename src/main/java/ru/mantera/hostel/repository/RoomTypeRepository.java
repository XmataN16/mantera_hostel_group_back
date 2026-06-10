package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.RoomType;

import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    List<RoomType> findByHotel_Id(Long hotelId);

    boolean existsByHotel_IdAndCodeIgnoreCase(Long hotelId, String code);

    Optional<RoomType> findByHotel_IdAndCodeIgnoreCase(Long hotelId, String code);
}