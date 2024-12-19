package com.sti.accounting.securityLayer.repository;

import com.sti.accounting.securityLayer.entities.UserRoleEntity;
import com.sti.accounting.securityLayer.entities.UserRoleKey;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRoleRepository extends ListCrudRepository<UserRoleEntity, UserRoleKey> {
}
