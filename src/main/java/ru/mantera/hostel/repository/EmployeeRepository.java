package ru.mantera.hostel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mantera.hostel.entity.Employee;
import ru.mantera.hostel.enums.EmployeeStatus;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByHotel_IdOrderByFullNameAsc(Long hotelId);

    List<Employee> findByStatusOrderByFullNameAsc(EmployeeStatus status);
}