package com.sti.accounting.securityLayer.repository;

import com.sti.accounting.securityLayer.entities.PermissionsEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPermissionRepository extends ListCrudRepository<PermissionsEntity, Long> {
}
