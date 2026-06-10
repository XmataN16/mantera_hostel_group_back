package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.Guest;

import java.util.List;
import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumber(String documentNumber);

    List<Guest> findByLastNameContainingIgnoreCase(String lastName);

    List<Guest> findByPhoneContaining(String phone);
}