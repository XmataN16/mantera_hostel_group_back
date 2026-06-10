// src/main/java/ru/mantera/hostel/service/RoleService.java
package ru.mantera.hostel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mantera.hostel.dto.role.RoleCreateRequest;
import ru.mantera.hostel.dto.role.RoleDto;
import ru.mantera.hostel.entity.Role;
import ru.mantera.hostel.exception.ConflictException;
import ru.mantera.hostel.exception.ResourceNotFoundException;
import ru.mantera.hostel.mapper.RoleMapper;
import ru.mantera.hostel.repository.RoleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<RoleDto> findAll() {
        return roleRepository.findAll()
                .stream()
                .map(RoleMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleDto findById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Роль с id=" + id + " не найдена"));
        return RoleMapper.toDto(role);
    }

    public RoleDto create(RoleCreateRequest request) {
        if (roleRepository.existsByCode(request.code())) {
            throw new ConflictException("Роль с кодом '" + request.code() + "' уже существует");
        }

        Role role = RoleMapper.toEntity(request);
        Role saved = roleRepository.save(role);
        return RoleMapper.toDto(saved);
    }
}