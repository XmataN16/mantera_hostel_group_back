package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.additionalservice.AdditionalServiceCreateRequest;
import ru.mantera.hostel.dto.additionalservice.AdditionalServiceResponse;
import ru.mantera.hostel.dto.additionalservice.AdditionalServiceUpdateRequest;
import ru.mantera.hostel.entity.AdditionalService;
import ru.mantera.hostel.entity.Hotel;
import ru.mantera.hostel.enums.ServiceStatus;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.AdditionalServiceRepository;
import ru.mantera.hostel.repository.HotelRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdditionalServiceService {

    private final AdditionalServiceRepository additionalServiceRepository;
    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    public List<AdditionalServiceResponse> findAll(Long hotelId, ServiceStatus status) {
        List<AdditionalService> services;

        if (hotelId != null && status != null) {
            services = additionalServiceRepository.findByHotel_IdAndStatusOrderByNameAsc(hotelId, status);
        } else if (hotelId != null) {
            services = additionalServiceRepository.findByHotel_IdOrderByNameAsc(hotelId);
        } else {
            services = additionalServiceRepository.findAll();
        }

        return services.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdditionalServiceResponse findById(Long id) {
        return toResponse(getServiceOrThrow(id));
    }

    public AdditionalServiceResponse create(AdditionalServiceCreateRequest request) {
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Отель с id=" + request.hotelId() + " не найден"
                ));

        AdditionalService service = AdditionalService.builder()
                .hotel(hotel)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .status(ServiceStatus.ACTIVE)
                .build();

        return toResponse(additionalServiceRepository.save(service));
    }

    public AdditionalServiceResponse update(Long id, AdditionalServiceUpdateRequest request) {
        AdditionalService service = getServiceOrThrow(id);

        if (request.name() != null) {
            service.setName(request.name());
        }

        if (request.description() != null) {
            service.setDescription(request.description());
        }

        if (request.price() != null) {
            service.setPrice(request.price());
        }

        if (request.status() != null) {
            service.setStatus(request.status());
        }

        return toResponse(additionalServiceRepository.save(service));
    }

    public void delete(Long id) {
        AdditionalService service = getServiceOrThrow(id);

        service.setStatus(ServiceStatus.INACTIVE);
        additionalServiceRepository.save(service);
    }

    private AdditionalService getServiceOrThrow(Long id) {
        return additionalServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Дополнительная услуга с id=" + id + " не найдена"
                ));
    }

    private AdditionalServiceResponse toResponse(AdditionalService service) {
        return new AdditionalServiceResponse(
                service.getId(),
                service.getHotel().getId(),
                service.getHotel().getName(),
                service.getName(),
                service.getDescription(),
                service.getPrice(),
                service.getStatus(),
                service.getCreatedAt(),
                service.getUpdatedAt()
        );
    }
}