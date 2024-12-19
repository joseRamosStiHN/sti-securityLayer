package com.sti.accounting.securityLayer.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "COMPANY_USER_ROLE")
@Getter
@Setter
public class CompanyUserRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id")
    Long companyId;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "permissions_id")
    Long permissionId;

    @ManyToOne
    @JoinColumn(name = "company_id", insertable = false, updatable = false, nullable = false)
    private CompanyEntity company;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "role_id", insertable = false, updatable = false, nullable = true)
    private RoleEntity role;

    @ManyToOne
    @JoinColumn(name = "permissions_id", insertable = false, updatable = false)
    private PermissionsEntity permissions;

    private String status; // ACTIVE, SUSPENDED

    @CreatedDate
    private LocalDateTime createdAt;
}
