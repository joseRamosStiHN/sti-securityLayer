package com.sti.accounting.securityLayer.repository;

import com.sti.accounting.securityLayer.entities.UserRoleEntity;
import com.sti.accounting.securityLayer.entities.UserRoleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleKey> {

    void deleteByUserIdAndRoleId(Long userId, Long roleId);

}
