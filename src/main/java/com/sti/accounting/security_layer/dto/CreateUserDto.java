package com.sti.accounting.security_layer.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDto extends UserDto{

    @NotNull
    private String password;

    @NotNull
    @Override
    public String getUserName() {
        return super.getUserName();
    }

    @NotNull
    @Override
    public String getEmail() {
        return super.getEmail();
    }

    @NotNull
    @Override
    public String getFirstName() {
        return super.getFirstName();
    }
    @NotNull
    @Override
    public String getLastName() {
        return super.getLastName();
    }


}
