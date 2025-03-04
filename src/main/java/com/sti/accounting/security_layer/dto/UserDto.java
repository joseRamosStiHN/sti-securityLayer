package com.sti.accounting.security_layer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long id;
    private String userName;
    private String firstName;
    private String lastName;
    private String userAddress;
    private String userPhone;
    private String email;
    private LocalDateTime createdAt;
    private boolean isActive;
    private List<KeyValueDto> globalRoles;
    private List<CompanyUserDto> companies;

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
