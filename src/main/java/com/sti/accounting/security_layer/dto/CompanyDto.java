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
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyDto {
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

    // Cambiamos a protected para que las clases hijas puedan sobrescribirlo
    protected String companyLogo;

    private List<CompanyUserDto> users;

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getCompanyLogo() {
        return this.companyLogo;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CompanyDto that = (CompanyDto) o;
        return isActive() == that.isActive()
                && Objects.equals(getId(), that.getId())
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getDescription(), that.getDescription())
                && Objects.equals(getAddress(), that.getAddress())
                && Objects.equals(getRtn(), that.getRtn())
                && getType() == that.getType()
                && Objects.equals(getEmail(), that.getEmail())
                && Objects.equals(getPhone(), that.getPhone())
                && Objects.equals(getWebsite(), that.getWebsite())
                && Objects.equals(getTenantId(), that.getTenantId())
                && Objects.equals(getCreatedAt(), that.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getAddress(), getRtn(),
                getType(), getEmail(), getPhone(), getWebsite(), isActive(),
                getTenantId(), getCreatedAt());
    }
}