package com.sti.accounting.security_layer.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@Entity
@Table(name = "ROLE")
@AllArgsConstructor
@NoArgsConstructor
public class RoleEntity {


    public RoleEntity(Long id, String roleName, Boolean isGlobal, String roleDescription) {
        this.id = id;
        this.roleName = roleName;
        this.isGlobal = isGlobal;
        this.roleDescription = roleDescription;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleName; //ADMIN, OPERATOR, AUDIT, SUPER_ADMIN

    private Boolean isGlobal;

    private String roleDescription;

    @OneToMany(mappedBy = "role")
    private Set<CompanyUserRoleEntity> rolUserCompanyEntity;

    public RoleEntity(Long id) {

    }
}
