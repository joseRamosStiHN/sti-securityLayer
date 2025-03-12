package com.sti.accounting.security_layer.repository;

import com.sti.accounting.security_layer.entities.CompanyEntity;
import com.sti.accounting.security_layer.entities.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUserRepository extends ListCrudRepository<UserEntity, Long> {

    UserEntity findByUserName(String userName);

    UserEntity findByUserNameAndPassword(String userName, String password);


    @Query("SELECT u FROM UserEntity u JOIN FETCH u.companyUser cru WHERE cru.company.id =:companyId and u.isActive = true and cru.status = 'ACTIVE'")
    List<UserEntity> getUsersByCompany(Long companyId);
}
