package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.CompanyUserRoleEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ICompanyUserRepository extends ListCrudRepository<CompanyUserRoleEntity, Long> {

    void deleteByUserIdAndCompanyIdAndRoleId(Long userId, Long companyId, Long roleId);

    void deleteByCompanyIdAndRoleId(Long companyId, Long roleId);

    List<CompanyUserRoleEntity> findByCompanyId(Long companyId);

    List<CompanyUserRoleEntity> findByCompanyIdAndUserId(Long companyId, Long userId);


}
