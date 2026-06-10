// src/main/java/ru/mantera/hostel/repository/RoleRepository.java
package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    boolean existsByCode(String code);
}