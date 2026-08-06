package com.portfoliomanager.service;

import com.portfoliomanager.model.AppUser;
import com.portfoliomanager.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Authenticated Google user is required");
        }

        String googleSubject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String displayName = jwt.getClaimAsString("name");
        String avatarUrl = jwt.getClaimAsString("picture");

        return appUserRepository.upsertByGoogleSubject(googleSubject, email, displayName, avatarUrl);
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
