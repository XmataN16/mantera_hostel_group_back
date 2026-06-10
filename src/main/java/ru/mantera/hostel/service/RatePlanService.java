package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.rate.RatePlanCreateRequest;
import ru.mantera.hostel.dto.rate.RatePlanResponse;
import ru.mantera.hostel.dto.rate.RatePlanUpdateRequest;
import ru.mantera.hostel.entity.Hotel;
import ru.mantera.hostel.entity.RatePlan;
import ru.mantera.hostel.enums.HotelStatus;
import ru.mantera.hostel.exception.ConflictException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.HotelRepository;
import ru.mantera.hostel.repository.RatePlanRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RatePlanService {

    private final RatePlanRepository ratePlanRepository;
    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    public List<RatePlanResponse> findAll(Long hotelId) {
        List<RatePlan> ratePlans = hotelId == null
                ? ratePlanRepository.findAll()
                : ratePlanRepository.findByHotel_IdOrderByNameAsc(hotelId);

        return ratePlans.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RatePlanResponse findById(Long id) {
        return toResponse(getRatePlanOrThrow(id));
    }

    public RatePlanResponse create(RatePlanCreateRequest request) {
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Отель с id=" + request.hotelId() + " не найден"
                ));

        if (ratePlanRepository.existsByHotel_IdAndCodeIgnoreCase(request.hotelId(), request.code())) {
            throw new ConflictException("Тариф с кодом '" + request.code() + "' уже существует в этом отеле");
        }

        RatePlan ratePlan = RatePlan.builder()
                .hotel(hotel)
                .code(request.code())
                .name(request.name())
                .description(request.description())
                .mealPlan(request.mealPlan())
                .refundable(request.refundable())
                .cancellationPolicy(request.cancellationPolicy())
                .status(HotelStatus.ACTIVE)
                .build();

        return toResponse(ratePlanRepository.save(ratePlan));
    }

    public RatePlanResponse update(Long id, RatePlanUpdateRequest request) {
        RatePlan ratePlan = getRatePlanOrThrow(id);

        if (request.code() != null && !request.code().equalsIgnoreCase(ratePlan.getCode())) {
            if (ratePlanRepository.existsByHotel_IdAndCodeIgnoreCase(ratePlan.getHotel().getId(), request.code())) {
                throw new ConflictException("Тариф с кодом '" + request.code() + "' уже существует в этом отеле");
            }
            ratePlan.setCode(request.code());
        }

        if (request.name() != null) {
            ratePlan.setName(request.name());
        }

        if (request.description() != null) {
            ratePlan.setDescription(request.description());
        }

        if (request.mealPlan() != null) {
            ratePlan.setMealPlan(request.mealPlan());
        }

        if (request.refundable() != null) {
            ratePlan.setRefundable(request.refundable());
        }

        if (request.cancellationPolicy() != null) {
            ratePlan.setCancellationPolicy(request.cancellationPolicy());
        }

        if (request.status() != null) {
            ratePlan.setStatus(request.status());
        }

        return toResponse(ratePlanRepository.save(ratePlan));
    }

    public void delete(Long id) {
        RatePlan ratePlan = getRatePlanOrThrow(id);
        ratePlan.setStatus(HotelStatus.INACTIVE);
        ratePlanRepository.save(ratePlan);
    }

    private RatePlan getRatePlanOrThrow(Long id) {
        return ratePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Тариф с id=" + id + " не найден"
                ));
    }

    private RatePlanResponse toResponse(RatePlan ratePlan) {
        return new RatePlanResponse(
                ratePlan.getId(),
                ratePlan.getHotel().getId(),
                ratePlan.getHotel().getName(),
                ratePlan.getCode(),
                ratePlan.getName(),
                ratePlan.getDescription(),
                ratePlan.getMealPlan(),
                ratePlan.getRefundable(),
                ratePlan.getCancellationPolicy(),
                ratePlan.getStatus(),
                ratePlan.getCreatedAt(),
                ratePlan.getUpdatedAt()
        );
    }
}