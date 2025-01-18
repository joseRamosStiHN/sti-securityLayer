package com.sti.accounting.security_layer.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_ROLE")
@Getter
@Setter
public class UserRoleEntity implements Serializable {

    @EmbeddedId
    UserRoleKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id",  nullable = false)
    private RoleEntity role;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
