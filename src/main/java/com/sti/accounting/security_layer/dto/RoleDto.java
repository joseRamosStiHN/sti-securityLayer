package com.sti.accounting.security_layer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private Long id;
    private String roleName;
    private boolean isGlobal;
    private String roleDescription;

    public void setIsGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }
}