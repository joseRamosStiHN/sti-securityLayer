package com.sti.accounting.security_layer.entities;


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

    private Boolean isGlobal;

    private String roleDescription;

    public RoleEntity(Long id) {

    }
}
