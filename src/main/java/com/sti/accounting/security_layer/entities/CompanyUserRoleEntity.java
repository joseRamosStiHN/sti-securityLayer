package com.sti.accounting.security_layer.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "COMPANY_USER_ROLE")
@Getter
@Setter
public class CompanyUserRoleEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id",  nullable = false)
    private CompanyEntity company;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

//    @ManyToOne
//    @JoinColumn(name = "permissions_id")
//    private PermissionsEntity permissions;

    private String status; // ACTIVE, SUSPENDED

    @CreatedDate
    private LocalDateTime createdAt;
}
