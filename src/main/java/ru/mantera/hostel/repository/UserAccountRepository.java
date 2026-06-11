package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.UserAccount;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmployee_Id(Long employeeId);
}