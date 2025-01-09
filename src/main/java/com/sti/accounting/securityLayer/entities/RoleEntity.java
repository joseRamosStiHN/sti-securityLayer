package com.sti.accounting.securityLayer.entities;


import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "ROLE")
@AllArgsConstructor
@NoArgsConstructor
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roleName; //ADMIN, OPERATOR, AUDIT, SUPER_ADMIN

    private boolean isGlobal;

    private String roleDescription;

    public RoleEntity(Long id) {

    }

    public void setIsGlobal(boolean global) {
    }
}
