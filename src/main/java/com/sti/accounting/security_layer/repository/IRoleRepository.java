package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.RoleEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRoleRepository extends ListCrudRepository<RoleEntity, Long> {

    @Query("select r from RoleEntity r JOIN FETCH r.rolUserCompanyEntity cur where cur.company.id = :companyId and cur.status = 'ACTIVE' and cur.user.id = :userId")
    List<RoleEntity> getRoleByCompany(Long companyId,Long userId);


}
