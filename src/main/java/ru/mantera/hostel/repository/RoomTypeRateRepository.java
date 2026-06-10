package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.mantera.hostel.entity.RoomTypeRate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomTypeRateRepository extends JpaRepository<RoomTypeRate, Long> {

    List<RoomTypeRate> findByRoomType_IdOrderByValidFromAsc(Long roomTypeId);

    List<RoomTypeRate> findByRatePlan_IdOrderByValidFromAsc(Long ratePlanId);

    @Query("""
            select rtr
            from RoomTypeRate rtr
            where rtr.roomType.id = :roomTypeId
              and rtr.ratePlan.id = :ratePlanId
              and rtr.validFrom <= :date
              and rtr.validTo >= :date
            order by rtr.validFrom desc
            """)
    Optional<RoomTypeRate> findActualRate(
            @Param("roomTypeId") Long roomTypeId,
            @Param("ratePlanId") Long ratePlanId,
            @Param("date") LocalDate date
    );

    @Query("""
            select count(rtr) > 0
            from RoomTypeRate rtr
            where rtr.roomType.id = :roomTypeId
              and rtr.ratePlan.id = :ratePlanId
              and rtr.validFrom <= :validTo
              and rtr.validTo >= :validFrom
              and (:excludeId is null or rtr.id <> :excludeId)
            """)
    boolean existsOverlappingPeriod(
            @Param("roomTypeId") Long roomTypeId,
            @Param("ratePlanId") Long ratePlanId,
            @Param("validFrom") LocalDate validFrom,
            @Param("validTo") LocalDate validTo,
            @Param("excludeId") Long excludeId
    );
}