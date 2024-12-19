package com.sti.accounting.securityLayer.repository;

import com.sti.accounting.securityLayer.entities.CompanyEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICompanyRepository extends ListCrudRepository<CompanyEntity, Long> {
}
