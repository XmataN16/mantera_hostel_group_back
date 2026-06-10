package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.AdditionalService;
import ru.mantera.hostel.enums.ServiceStatus;

import java.util.List;

public interface AdditionalServiceRepository extends JpaRepository<AdditionalService, Long> {

    List<AdditionalService> findByHotel_IdOrderByNameAsc(Long hotelId);

    List<AdditionalService> findByHotel_IdAndStatusOrderByNameAsc(
            Long hotelId,
            ServiceStatus status
    );
}