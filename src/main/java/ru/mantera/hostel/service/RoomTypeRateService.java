package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.rate.RoomTypeRateCreateRequest;
import ru.mantera.hostel.dto.rate.RoomTypeRateResponse;
import ru.mantera.hostel.dto.rate.RoomTypeRateUpdateRequest;
import ru.mantera.hostel.entity.RatePlan;
import ru.mantera.hostel.entity.RoomType;
import ru.mantera.hostel.entity.RoomTypeRate;
import ru.mantera.hostel.exception.BadRequestException;
import ru.mantera.hostel.exception.ConflictException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.RatePlanRepository;
import ru.mantera.hostel.repository.RoomTypeRateRepository;
import ru.mantera.hostel.repository.RoomTypeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomTypeRateService {

    private final RoomTypeRateRepository roomTypeRateRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RatePlanRepository ratePlanRepository;

    @Transactional(readOnly = true)
    public List<RoomTypeRateResponse> findAll(Long roomTypeId, Long ratePlanId) {
        List<RoomTypeRate> rates;

        if (roomTypeId != null) {
            rates = roomTypeRateRepository.findByRoomType_IdOrderByValidFromAsc(roomTypeId);
        } else if (ratePlanId != null) {
            rates = roomTypeRateRepository.findByRatePlan_IdOrderByValidFromAsc(ratePlanId);
        } else {
            rates = roomTypeRateRepository.findAll();
        }

        return rates.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomTypeRateResponse findById(Long id) {
        return toResponse(getRoomTypeRateOrThrow(id));
    }

    public RoomTypeRateResponse create(RoomTypeRateCreateRequest request) {
        validateDates(request.validFrom(), request.validTo());

        RoomType roomType = roomTypeRepository.findById(request.roomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Категория номера с id=" + request.roomTypeId() + " не найдена"
                ));

        RatePlan ratePlan = ratePlanRepository.findById(request.ratePlanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Тариф с id=" + request.ratePlanId() + " не найден"
                ));

        if (!roomType.getHotel().getId().equals(ratePlan.getHotel().getId())) {
            throw new BadRequestException("Категория номера и тариф относятся к разным отелям");
        }

        if (roomTypeRateRepository.existsOverlappingPeriod(
                request.roomTypeId(),
                request.ratePlanId(),
                request.validFrom(),
                request.validTo(),
                null
        )) {
            throw new ConflictException("Для этой категории и тарифа уже есть цена на пересекающийся период");
        }

        RoomTypeRate roomTypeRate = RoomTypeRate.builder()
                .roomType(roomType)
                .ratePlan(ratePlan)
                .validFrom(request.validFrom())
                .validTo(request.validTo())
                .price(request.price())
                .build();

        return toResponse(roomTypeRateRepository.save(roomTypeRate));
    }

    public RoomTypeRateResponse update(Long id, RoomTypeRateUpdateRequest request) {
        RoomTypeRate roomTypeRate = getRoomTypeRateOrThrow(id);

        LocalDate newValidFrom = request.validFrom() != null
                ? request.validFrom()
                : roomTypeRate.getValidFrom();

        LocalDate newValidTo = request.validTo() != null
                ? request.validTo()
                : roomTypeRate.getValidTo();

        validateDates(newValidFrom, newValidTo);

        if (roomTypeRateRepository.existsOverlappingPeriod(
                roomTypeRate.getRoomType().getId(),
                roomTypeRate.getRatePlan().getId(),
                newValidFrom,
                newValidTo,
                roomTypeRate.getId()
        )) {
            throw new ConflictException("Для этой категории и тарифа уже есть цена на пересекающийся период");
        }

        roomTypeRate.setValidFrom(newValidFrom);
        roomTypeRate.setValidTo(newValidTo);

        if (request.price() != null) {
            roomTypeRate.setPrice(request.price());
        }

        return toResponse(roomTypeRateRepository.save(roomTypeRate));
    }

    public void delete(Long id) {
        if (!roomTypeRateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Сезонная цена с id=" + id + " не найдена");
        }

        roomTypeRateRepository.deleteById(id);
    }

    private RoomTypeRate getRoomTypeRateOrThrow(Long id) {
        return roomTypeRateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Сезонная цена с id=" + id + " не найдена"
                ));
    }

    private void validateDates(LocalDate validFrom, LocalDate validTo) {
        if (validFrom == null || validTo == null) {
            throw new BadRequestException("Даты действия тарифа обязательны");
        }

        if (validTo.isBefore(validFrom)) {
            throw new BadRequestException("Дата окончания тарифа не может быть раньше даты начала");
        }
    }

    private RoomTypeRateResponse toResponse(RoomTypeRate roomTypeRate) {
        return new RoomTypeRateResponse(
                roomTypeRate.getId(),
                roomTypeRate.getRoomType().getId(),
                roomTypeRate.getRoomType().getName(),
                roomTypeRate.getRatePlan().getId(),
                roomTypeRate.getRatePlan().getName(),
                roomTypeRate.getValidFrom(),
                roomTypeRate.getValidTo(),
                roomTypeRate.getPrice(),
                roomTypeRate.getCreatedAt()
        );
    }
}