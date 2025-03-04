package com.sti.accounting.security_layer.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "COMPANY_USER_ROLE_AUDIT")
@Getter
@Setter
public class CompanyUserRoleAuditEntity extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyEntity company;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    private String action; // "ADDED", "REMOVED", "ROLE_CHANGED"
    private String previousStatus;
    private String newStatus;
    private LocalDateTime actionDate;
    private Long actionByUser;
}

