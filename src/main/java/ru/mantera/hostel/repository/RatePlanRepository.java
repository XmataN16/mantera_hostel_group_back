package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.RatePlan;

import java.util.List;

public interface RatePlanRepository extends JpaRepository<RatePlan, Long> {

    List<RatePlan> findByHotel_IdOrderByNameAsc(Long hotelId);

    boolean existsByHotel_IdAndCodeIgnoreCase(Long hotelId, String code);
}