package com.sti.accounting.security_layer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {

    @NotNull
    private String userName;
    @NotNull
    private String password;
}
