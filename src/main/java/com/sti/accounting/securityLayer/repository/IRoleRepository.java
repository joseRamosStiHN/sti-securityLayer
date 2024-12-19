package com.sti.accounting.securityLayer.repository;

import com.sti.accounting.securityLayer.entities.RoleEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRoleRepository extends ListCrudRepository<RoleEntity, Long> {
}
