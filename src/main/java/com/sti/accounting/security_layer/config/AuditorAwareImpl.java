package com.sti.accounting.security_layer.config;

import com.sti.accounting.security_layer.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;


@AllArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {

    private final AuthService authService;

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(this.authService.getUsername());
    }
}
