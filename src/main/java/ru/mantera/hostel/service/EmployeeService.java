package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.employee.EmployeeCreateRequest;
import ru.mantera.hostel.dto.employee.EmployeeResponse;
import ru.mantera.hostel.dto.employee.EmployeeUpdateRequest;
import ru.mantera.hostel.entity.Employee;
import ru.mantera.hostel.entity.Hotel;
import ru.mantera.hostel.enums.EmployeeStatus;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.EmployeeRepository;
import ru.mantera.hostel.repository.HotelRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll(Long hotelId, EmployeeStatus status) {
        List<Employee> employees;

        if (hotelId != null) {
            employees = employeeRepository.findByHotel_IdOrderByFullNameAsc(hotelId);
        } else if (status != null) {
            employees = employeeRepository.findByStatusOrderByFullNameAsc(status);
        } else {
            employees = employeeRepository.findAll();
        }

        return employees.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        return toResponse(getEmployeeOrThrow(id));
    }

    public EmployeeResponse create(EmployeeCreateRequest request) {
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Отель с id=" + request.hotelId() + " не найден"
                ));

        Employee employee = Employee.builder()
                .hotel(hotel)
                .fullName(request.fullName())
                .position(request.position())
                .phone(request.phone())
                .email(request.email())
                .status(EmployeeStatus.ACTIVE)
                .build();

        return toResponse(employeeRepository.save(employee));
    }

    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        Employee employee = getEmployeeOrThrow(id);

        if (request.hotelId() != null) {
            Hotel hotel = hotelRepository.findById(request.hotelId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Отель с id=" + request.hotelId() + " не найден"
                    ));
            employee.setHotel(hotel);
        }

        if (request.fullName() != null) {
            employee.setFullName(request.fullName());
        }

        if (request.position() != null) {
            employee.setPosition(request.position());
        }

        if (request.phone() != null) {
            employee.setPhone(request.phone());
        }

        if (request.email() != null) {
            employee.setEmail(request.email());
        }

        if (request.status() != null) {
            employee.setStatus(request.status());
        }

        return toResponse(employeeRepository.save(employee));
    }

    public void delete(Long id) {
        Employee employee = getEmployeeOrThrow(id);
        employee.setStatus(EmployeeStatus.DISMISSED);
        employeeRepository.save(employee);
    }

    private Employee getEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Сотрудник с id=" + id + " не найден"
                ));
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getHotel().getId(),
                employee.getHotel().getName(),
                employee.getFullName(),
                employee.getPosition(),
                employee.getPhone(),
                employee.getEmail(),
                employee.getStatus(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}