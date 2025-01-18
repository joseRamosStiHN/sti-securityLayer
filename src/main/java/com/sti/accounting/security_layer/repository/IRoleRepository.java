package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.RoleEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRoleRepository extends ListCrudRepository<RoleEntity, Long> {
}
