package com.music.JunStudio.upload.security;


import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves current authenticated user ID for upload ownership.
 * Replace fallback behavior if your app requires strict auth only.
 */
@Component
public class UploadAuthHelper {

    public String currentUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Unauthenticated user cannot upload files.");
        }

        // Prefer stable principal name. Customize if you store user ID in JWT claims.
        String userId = auth.getName();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Unable to resolve authenticated user id.");
        }
        return userId;
    }
}