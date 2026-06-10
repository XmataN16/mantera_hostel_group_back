package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}