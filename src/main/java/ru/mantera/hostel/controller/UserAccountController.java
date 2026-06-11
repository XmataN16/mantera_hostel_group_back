package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.user.*;
import ru.mantera.hostel.service.UserAccountService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    @GetMapping
    public List<UserAccountResponse> getAll() {
        return userAccountService.findAll();
    }

    @GetMapping("/{id}")
    public UserAccountResponse getById(@PathVariable Long id) {
        return userAccountService.findById(id);
    }

    @PostMapping
    public UserAccountResponse create(@RequestBody @Valid UserAccountCreateRequest request) {
        return userAccountService.create(request);
    }

    @PutMapping("/{id}")
    public UserAccountResponse update(
            @PathVariable Long id,
            @RequestBody @Valid UserAccountUpdateRequest request
    ) {
        return userAccountService.update(id, request);
    }

    @PatchMapping("/{id}/password")
    public UserAccountResponse changePassword(
            @PathVariable Long id,
            @RequestBody @Valid UserAccountPasswordUpdateRequest request
    ) {
        return userAccountService.changePassword(id, request);
    }

    @PatchMapping("/{id}/roles")
    public UserAccountResponse changeRoles(
            @PathVariable Long id,
            @RequestBody @Valid UserAccountRolesUpdateRequest request
    ) {
        return userAccountService.changeRoles(id, request);
    }

    @PatchMapping("/{id}/enabled")
    public UserAccountResponse changeEnabled(
            @PathVariable Long id,
            @RequestBody @Valid UserAccountEnabledUpdateRequest request
    ) {
        return userAccountService.changeEnabled(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userAccountService.delete(id);
    }
}