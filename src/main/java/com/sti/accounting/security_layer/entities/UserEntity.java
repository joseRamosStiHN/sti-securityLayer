package com.sti.accounting.security_layer.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "USER")
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userName;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    private String userAddress;
    private String userPhone;
    private String email;
    @Column(nullable = false)
    private String password;

    private Boolean isActive;

    @OneToMany(mappedBy = "user")
    private Set<CompanyUserRoleEntity> companyUser;

    @OneToMany(mappedBy = "user")
    private Set<UserRoleEntity> userRoles;

    @CreatedDate
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public UserEntity(Long id) {
        this.id = id;
    }

    // Método para obtener los roles globales
    public Set<RoleEntity> getGlobalRoles() {
        return userRoles.stream()
                .map(UserRoleEntity::getRole)
                .collect(Collectors.toSet());
    }
}
