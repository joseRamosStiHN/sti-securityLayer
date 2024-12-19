package com.sti.accounting.securityLayer.repository;

import com.sti.accounting.securityLayer.entities.UserEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends ListCrudRepository<UserEntity, Long> {

    UserEntity findByUserName(String userName);

    UserEntity findByUserNameAndPassword(String userName, String password);
}
