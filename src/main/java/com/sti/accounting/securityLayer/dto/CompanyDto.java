package com.sti.accounting.securityLayer.dto;

import com.sti.accounting.securityLayer.utils.CompanyTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    private Set<KeyValueDto> roles;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CompanyDto that = (CompanyDto) o;
        return isActive() == that.isActive() && Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getAddress(), that.getAddress()) && Objects.equals(getRtn(), that.getRtn()) && getType() == that.getType() && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getPhone(), that.getPhone()) && Objects.equals(getWebsite(), that.getWebsite()) && Objects.equals(getTenantId(), that.getTenantId()) && Objects.equals(getCreatedAt(), that.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription(), getAddress(), getRtn(), getType(), getEmail(), getPhone(), getWebsite(), isActive(), getTenantId(), getCreatedAt());
    }


}
