package com.sti.accounting.securityLayer.repository;

import com.sti.accounting.securityLayer.entities.CompanyUserRoleEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ICompanyUserRepository extends ListCrudRepository<CompanyUserRoleEntity, Long> {

    void deleteByUserIdAndCompanyIdAndRoleId(Long userId, Long companyId, Long roleId);

    List<CompanyUserRoleEntity> findByCompanyId(Long companyId);


}
