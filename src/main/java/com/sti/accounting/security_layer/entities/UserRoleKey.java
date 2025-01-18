package com.sti.accounting.security_layer.entities;


import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleKey implements Serializable {

   private Long userId;

   private Long roleId;


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleKey that = (UserRoleKey) o;
        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}
