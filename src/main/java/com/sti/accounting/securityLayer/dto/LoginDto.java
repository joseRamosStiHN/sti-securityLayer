package com.sti.accounting.securityLayer.dto;

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
