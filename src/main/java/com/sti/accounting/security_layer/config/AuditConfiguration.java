package com.sti.accounting.security_layer.config;


import com.sti.accounting.security_layer.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class AuditConfiguration {


    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl(new AuthService());
    }
}
