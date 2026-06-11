package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.user.*;
import ru.mantera.hostel.entity.Employee;
import ru.mantera.hostel.entity.Role;
import ru.mantera.hostel.entity.UserAccount;
import ru.mantera.hostel.exception.BadRequestException;
import ru.mantera.hostel.exception.ConflictException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.repository.EmployeeRepository;
import ru.mantera.hostel.repository.RoleRepository;
import ru.mantera.hostel.repository.UserAccountRepository;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserAccountResponse> findAll() {
        return userAccountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserAccountResponse findById(Long id) {
        return toResponse(getUserOrThrow(id));
    }

    public UserAccountResponse create(UserAccountCreateRequest request) {
        if (userAccountRepository.existsByUsername(request.username())) {
            throw new ConflictException("Пользователь с username='" + request.username() + "' уже существует");
        }

        if (userAccountRepository.existsByEmployee_Id(request.employeeId())) {
            throw new ConflictException("Для сотрудника id=" + request.employeeId() + " уже создана учётная запись");
        }

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Сотрудник с id=" + request.employeeId() + " не найден"
                ));

        Set<Role> roles = resolveRoles(request.roleCodes());

        UserAccount account = UserAccount.builder()
                .employee(employee)
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .enabled(true)
                .roles(roles)
                .build();

        return toResponse(userAccountRepository.save(account));
    }

    public UserAccountResponse update(Long id, UserAccountUpdateRequest request) {
        UserAccount account = getUserOrThrow(id);

        if (request.username() != null && !request.username().equalsIgnoreCase(account.getUsername())) {
            if (userAccountRepository.existsByUsername(request.username())) {
                throw new ConflictException("Пользователь с username='" + request.username() + "' уже существует");
            }

            account.setUsername(request.username());
        }

        if (request.enabled() != null) {
            account.setEnabled(request.enabled());
        }

        return toResponse(userAccountRepository.save(account));
    }

    public UserAccountResponse changePassword(Long id, UserAccountPasswordUpdateRequest request) {
        UserAccount account = getUserOrThrow(id);
        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        return toResponse(userAccountRepository.save(account));
    }

    public UserAccountResponse changeRoles(Long id, UserAccountRolesUpdateRequest request) {
        UserAccount account = getUserOrThrow(id);

        Set<Role> roles = resolveRoles(request.roleCodes());

        account.getRoles().clear();
        account.getRoles().addAll(roles);

        return toResponse(userAccountRepository.save(account));
    }

    public UserAccountResponse changeEnabled(Long id, UserAccountEnabledUpdateRequest request) {
        UserAccount account = getUserOrThrow(id);
        account.setEnabled(request.enabled());
        return toResponse(userAccountRepository.save(account));
    }

    public void delete(Long id) {
        if (!userAccountRepository.existsById(id)) {
            throw new ResourceNotFoundException("Пользователь с id=" + id + " не найден");
        }

        userAccountRepository.deleteById(id);
    }

    private UserAccount getUserOrThrow(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Пользователь с id=" + id + " не найден"
                ));
    }

    private Set<Role> resolveRoles(Set<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new BadRequestException("Нужно указать хотя бы одну роль");
        }

        Set<String> normalizedCodes = new HashSet<>();

        for (String code : roleCodes) {
            if (code != null && !code.isBlank()) {
                normalizedCodes.add(code.trim().toUpperCase());
            }
        }

        if (normalizedCodes.isEmpty()) {
            throw new BadRequestException("Коды ролей не должны быть пустыми");
        }

        List<Role> roles = roleRepository.findByCodeIn(normalizedCodes);

        Set<String> foundCodes = roles.stream()
                .map(Role::getCode)
                .collect(java.util.stream.Collectors.toSet());

        Set<String> missingCodes = new HashSet<>(normalizedCodes);
        missingCodes.removeAll(foundCodes);

        if (!missingCodes.isEmpty()) {
            throw new BadRequestException("Не найдены роли: " + missingCodes);
        }

        return new LinkedHashSet<>(roles);
    }

    private UserAccountResponse toResponse(UserAccount account) {
        List<String> roles = account.getRoles()
                .stream()
                .map(Role::getCode)
                .sorted()
                .toList();

        return new UserAccountResponse(
                account.getId(),
                account.getEmployee().getId(),
                account.getEmployee().getFullName(),
                account.getUsername(),
                account.getEnabled(),
                roles,
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}