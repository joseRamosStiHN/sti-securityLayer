package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.CompanyEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICompanyRepository extends ListCrudRepository<CompanyEntity, Long> {
}
