package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.CompanyUserRoleAuditEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICompanyUserRoleAuditRepository extends ListCrudRepository<CompanyUserRoleAuditEntity, Long> {
}
