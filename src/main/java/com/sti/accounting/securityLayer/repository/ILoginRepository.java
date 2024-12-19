package com.sti.accounting.securityLayer.repository;

import com.sti.accounting.securityLayer.entities.LoginEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ILoginRepository extends ListCrudRepository<LoginEntity, Long> {
}
