package com.sti.accounting.securityLayer.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ROLE")
@AllArgsConstructor
@NoArgsConstructor
public class RoleEntity {

    public RoleEntity(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String roleName; //ADMIN, OPERATOR, AUDIT, SUPER_ADMIN
    private boolean isGlobal;
    private String roleDescription;

}
