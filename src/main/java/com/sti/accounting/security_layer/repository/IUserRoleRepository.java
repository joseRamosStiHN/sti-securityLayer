package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.UserRoleEntity;
import com.sti.accounting.security_layer.entities.UserRoleKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleKey> {

    List<UserRoleEntity> findByUserId(Long userId);

    void deleteByUserIdAndRoleId(Long userId, Long roleId);

}
