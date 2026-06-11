package ru.mantera.hostel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.mantera.hostel.dto.auth.JwtLoginResponse;
import ru.mantera.hostel.dto.auth.LoginRequest;
import ru.mantera.hostel.dto.user.CurrentUserResponse;
import ru.mantera.hostel.service.JwtService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public JwtLoginResponse login(@RequestBody @Valid LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        List<String> roles = jwtService.extractRolesFromAuthorities(
                authentication.getAuthorities()
        );

        String token = jwtService.generateToken(
                authentication.getName(),
                roles
        );

        return new JwtLoginResponse(
                "Bearer",
                token,
                authentication.getName(),
                roles
        );
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        List<String> roles = jwtService.extractRolesFromAuthorities(
                authentication.getAuthorities()
        );

        return new CurrentUserResponse(
                authentication.getName(),
                roles
        );
    }
}