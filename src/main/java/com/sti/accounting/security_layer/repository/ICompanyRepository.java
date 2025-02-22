package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.CompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICompanyRepository extends ListCrudRepository<CompanyEntity, Long> {


    @Query("SELECT c FROM CompanyEntity c WHERE EXISTS (SELECT cru FROM c.companyUserEntity cru WHERE cru.user.id = :userId and cru.status = 'ACTIVE' ) And c.status = 'ACTIVE'")
    Page<CompanyEntity> findCompanyByUser(Long userId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM CompanyEntity c JOIN c.companyUserEntity cru WHERE cru.company.id = c.id AND cru.user.id = :userId")
    long countCompaniesByUser(Long userId);

}


