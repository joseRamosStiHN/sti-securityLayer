package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.PermissionsEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPermissionRepository extends ListCrudRepository<PermissionsEntity, Long> {
}
