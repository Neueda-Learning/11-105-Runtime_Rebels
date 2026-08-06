package com.portfoliomanager.service;

import com.portfoliomanager.model.AppUser;
import com.portfoliomanager.repository.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    // Returns the seeded legacy user for local authentication.
    public AppUser getCurrentUser() {
        return appUserRepository.findByGoogleSubject("legacy-single-user")
                .orElseGet(() -> appUserRepository.upsertByGoogleSubject(
                        "legacy-single-user", "dev@example.local", "Dev User", null));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
