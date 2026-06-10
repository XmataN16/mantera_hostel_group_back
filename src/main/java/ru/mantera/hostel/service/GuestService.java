package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.guest.*;
import ru.mantera.hostel.entity.Guest;
import ru.mantera.hostel.exception.ConflictException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.GuestRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GuestService {

    private final GuestRepository guestRepository;

    @Transactional(readOnly = true)
    public List<GuestResponse> getAll() {
        return guestRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GuestResponse getById(Long id) {

        Guest guest = guestRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Guest not found: " + id));

        return toResponse(guest);
    }

    public GuestResponse create(GuestCreateRequest request) {

        if (guestRepository.existsByDocumentNumber(
                request.documentNumber())) {

            throw new ConflictException(
                    "Guest with document "
                            + request.documentNumber()
                            + " already exists");
        }

        Guest guest = Guest.builder()
                .lastName(request.lastName())
                .firstName(request.firstName())
                .middleName(request.middleName())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .phone(request.phone())
                .email(request.email())
                .citizenship(request.citizenship())
                .documentType(request.documentType())
                .documentNumber(request.documentNumber())
                .documentIssueDate(request.documentIssueDate())
                .documentIssuedBy(request.documentIssuedBy())
                .address(request.address())
                .comment(request.comment())
                .build();

        return toResponse(
                guestRepository.save(guest)
        );
    }

    public GuestResponse update(
            Long id,
            GuestUpdateRequest request) {

        Guest guest = guestRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Guest not found: " + id));

        if (request.lastName() != null)
            guest.setLastName(request.lastName());

        if (request.firstName() != null)
            guest.setFirstName(request.firstName());

        if (request.middleName() != null)
            guest.setMiddleName(request.middleName());

        if (request.birthDate() != null)
            guest.setBirthDate(request.birthDate());

        if (request.gender() != null)
            guest.setGender(request.gender());

        if (request.phone() != null)
            guest.setPhone(request.phone());

        if (request.email() != null)
            guest.setEmail(request.email());

        if (request.citizenship() != null)
            guest.setCitizenship(request.citizenship());

        if (request.documentType() != null)
            guest.setDocumentType(request.documentType());

        if (request.documentNumber() != null)
            guest.setDocumentNumber(request.documentNumber());

        if (request.documentIssueDate() != null)
            guest.setDocumentIssueDate(request.documentIssueDate());

        if (request.documentIssuedBy() != null)
            guest.setDocumentIssuedBy(request.documentIssuedBy());

        if (request.address() != null)
            guest.setAddress(request.address());

        if (request.comment() != null)
            guest.setComment(request.comment());

        return toResponse(
                guestRepository.save(guest)
        );
    }

    public void delete(Long id) {

        if (!guestRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Guest not found: " + id);
        }

        guestRepository.deleteById(id);
    }

    private GuestResponse toResponse(Guest guest) {

        return new GuestResponse(
                guest.getId(),
                guest.getLastName(),
                guest.getFirstName(),
                guest.getMiddleName(),
                guest.getBirthDate(),
                guest.getGender(),
                guest.getPhone(),
                guest.getEmail(),
                guest.getCitizenship(),
                guest.getDocumentType(),
                guest.getDocumentNumber(),
                guest.getDocumentIssueDate(),
                guest.getDocumentIssuedBy(),
                guest.getAddress(),
                guest.getComment(),
                guest.getCreatedAt(),
                guest.getUpdatedAt()
        );
    }
}