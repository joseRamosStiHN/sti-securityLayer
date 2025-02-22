package com.sti.accounting.security_layer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sti.accounting.security_layer.utils.CompanyTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyPaginationDto {


    private Long id;

    @NotNull
    private String name;
    private String description;
    private String address;

    @NotNull
    private String rtn;

    @NotNull
    private CompanyTypeEnum type;
    private String email;
    private String phone;
    private String website;
    private boolean isActive;
    private String tenantId;
    private LocalDate createdAt;

    private List<KeyValueDto> roles;

}
