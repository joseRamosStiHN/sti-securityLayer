package com.sti.accounting.security_layer.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "LOGIN")
@Getter
@Setter
public class LoginEntity {

    @Id
    private Long id;
    private String username;
    private boolean isLogin;
    private String ipAddress;
    private String metadata;
    @CreatedDate
    private LocalDateTime loginTime;
}
