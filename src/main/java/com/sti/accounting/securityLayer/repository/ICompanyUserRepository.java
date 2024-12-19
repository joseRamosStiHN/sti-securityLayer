package com.sti.accounting.securityLayer.repository;

import com.sti.accounting.securityLayer.entities.CompanyUserRoleEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ICompanyUserRepository extends ListCrudRepository<CompanyUserRoleEntity, Long> {
}
