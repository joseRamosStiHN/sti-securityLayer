package com.sti.accounting.securityLayer.entities;


import com.sti.accounting.securityLayer.utils.CompanyTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;


import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "COMPANY")
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String companyName;
    private String companyDescription;
    private String status;
    private String companyRTN;
    private String companyAddress;
    private CompanyTypeEnum type;
    private byte[] companyLogo;
    private String companyEmail;
    private String companyPhone;
    private String companyWebsite;
    private Boolean isActive;
    private String tenantId;

    @OneToMany(mappedBy = "company")
    private Set<CompanyUserRoleEntity> companyUserEntity;

    @CreatedDate
    private LocalDateTime createdAt;
    @CreatedDate
    private LocalDateTime updatedAt;

    public CompanyEntity(Long id) {
        this.id = id;
    }
}
