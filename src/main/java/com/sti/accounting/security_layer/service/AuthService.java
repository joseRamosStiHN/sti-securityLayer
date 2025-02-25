package com.sti.accounting.security_layer.service;


import com.sti.accounting.security_layer.core.CustomUserDetails;
import com.sti.accounting.security_layer.dto.KeyValueDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    public CustomUserDetails getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }

        throw new RuntimeException("Usuario no autenticado o no válido");
    }

    public String getUsername() {
        return getAuthenticatedUser().getUser().getUserName();
    }

    public Long getUserId() {
        return getAuthenticatedUser().getUser().getId();
    }

    public List<KeyValueDto> getUserRoles() {
        return getAuthenticatedUser().getUser().getGlobalRoles();
    }

    public boolean hasRole(String roleName) {
        return getAuthenticatedUser().getUser().getGlobalRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
}
