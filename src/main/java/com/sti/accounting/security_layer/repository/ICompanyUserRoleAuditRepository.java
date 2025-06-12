package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.CompanyUserRoleAuditEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ICompanyUserRoleAuditRepository extends ListCrudRepository<CompanyUserRoleAuditEntity, Long> {

    @Modifying
    @Query("DELETE FROM CompanyUserRoleAuditEntity a WHERE a.company.id = :companyId")
    void deleteByCompanyId(@Param("companyId") Long companyId);
}
