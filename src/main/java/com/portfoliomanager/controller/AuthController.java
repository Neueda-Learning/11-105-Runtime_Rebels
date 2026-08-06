package com.portfoliomanager.controller;

import com.portfoliomanager.dto.AuthUserResponse;
import com.portfoliomanager.model.AppUser;
import com.portfoliomanager.service.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authenticated user profile")
public class AuthController {

    private final CurrentUserService currentUserService;

    public AuthController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @Operation(summary = "Get the currently authenticated Google user")
    @GetMapping("/me")
    public AuthUserResponse me() {
        AppUser user = currentUserService.getCurrentUser();
        return AuthUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
