package com.sti.accounting.security_layer.service;

import com.sti.accounting.security_layer.core.CustomUserDetails;
import com.sti.accounting.security_layer.dto.KeyValueDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

//    public CustomUserDetails getAuthenticatedUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
//            return (CustomUserDetails) authentication.getPrincipal();
//        }
//
//        throw new RuntimeException("Unauthenticated or invalid user");
//    }
//
//    public String getUsername() {
//        return getAuthenticatedUser().getUser().getUserName();
//    }
//
//    public Long getUserId() {
//        return getAuthenticatedUser().getUser().getId();
//    }
//
//    public List<KeyValueDto> getUserRoles() {
//        return getAuthenticatedUser().getUser().getGlobalRoles();
//    }
//
//    public boolean hasRole(String roleName) {
//        return getAuthenticatedUser().getUser().getGlobalRoles().stream()
//                .anyMatch(role -> role.getName().equals(roleName));
//    }

    public CustomUserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }

        // During seeding or system operations, return null instead of throwing exception
        return null;
    }

    public String getUsername() {
        CustomUserDetails user = getAuthenticatedUser();
        return user != null ? user.getUser().getUserName() : "SYSTEM";
    }

    public Long getUserId() {
        CustomUserDetails user = getAuthenticatedUser();
        return user != null ? user.getUser().getId() : 0L;
    }

    public List<KeyValueDto> getUserRoles() {
        CustomUserDetails user = getAuthenticatedUser();
        return user != null ? user.getUser().getGlobalRoles() : List.of();
    }

    public boolean hasRole(String roleName) {
        CustomUserDetails user = getAuthenticatedUser();
        return user != null && user.getUser().getGlobalRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
}
