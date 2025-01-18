package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.LoginEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ILoginRepository extends ListCrudRepository<LoginEntity, Long> {
}
