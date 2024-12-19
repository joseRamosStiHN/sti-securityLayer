package com.sti.accounting.securityLayer.service;

import com.sti.accounting.securityLayer.core.Argon2Cipher;
import com.sti.accounting.securityLayer.dto.CompanyDto;
import com.sti.accounting.securityLayer.dto.CreateUserDto;
import com.sti.accounting.securityLayer.dto.KeyValueDto;
import com.sti.accounting.securityLayer.dto.UserDto;
import com.sti.accounting.securityLayer.entities.*;
import com.sti.accounting.securityLayer.repository.IUserRepository;
import com.sti.accounting.securityLayer.repository.IUserRoleRepository;
import com.sti.accounting.securityLayer.utils.TypeSMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;
    private final Argon2Cipher argon2Cipher;
    private final NotificationService notificationService;
    public UserService(IUserRepository userRepository, IUserRoleRepository userRoleRepository, Argon2Cipher argon2Cipher, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.argon2Cipher = argon2Cipher;
        this.notificationService = notificationService;
    }





    public List<UserDto> getAllUsers() {
        log.info("Get all users");
       return userRepository.findAll().stream().map(x->{
            UserDto dto = new UserDto();
            dto.setId(x.getId());
            dto.setFirstName(x.getFirstName());
            dto.setLastName(x.getLastName());
            dto.setEmail(x.getEmail());
            return dto;
        }).toList();
    }

    public UserDto getUserByUserNameAndPassword(String userName, String password) {
        log.info("Get user by username {}", userName);
        UserEntity entity = userRepository.findByUserName(userName);

        if(entity == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or Password not match");
        }

        if(!argon2Cipher.matches( password, entity.getPassword())){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or Password not match");
        }


        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setUserName(entity.getUserName());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        // al filtrar que solo sean los globales, esto hace que a fuerzas el isGlobal sea true
        List<KeyValueDto> globalRoles = entity.getUserRoles().stream()
                                        .filter(f -> f.getRole().isGlobal())
                                        .map(x -> new KeyValueDto(x.getRole().getId(), x.getRole().getRoleName(),
                                                              x.getRole().getRoleDescription(), true)).toList();
        dto.setRoles(globalRoles);
        dto.setActive(entity.getStatus().equalsIgnoreCase("A"));
        //set companies
        List<CompanyDto> companyDtoList = entity.getCompanyUser().stream().map(x -> getCompanyDto(x.getCompany())).toList();
        dto.setCompanies(companyDtoList);

        return dto;
    }

    public UserDto getUserById(Long id) {
        log.info("Get user by id {}", id);
        UserEntity entity = userRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setUserName(entity.getUserName());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        // al filtrar que solo sean los globales, esto hace que a fuerzas el isGlobal sea true
        List<KeyValueDto> globalRoles = entity.getUserRoles().stream()
                                        .filter(f -> f.getRole().isGlobal())
                                        .map(x -> new KeyValueDto(x.getRole().getId(),
                                                               x.getRole().getRoleName(), x.getRole().getRoleDescription(), true)).toList();
        dto.setRoles(globalRoles);
        dto.setActive(entity.getStatus().equalsIgnoreCase("A"));
        //set companies
        List<CompanyDto> companyDtoList = entity.getCompanyUser().stream().map(x -> getCompanyDto(x.getCompany())).toList();
        dto.setCompanies(companyDtoList);

        return dto;
    }

    public void createUser(CreateUserDto userDto) {
        log.info("Create user");

        // check if user already exists
        UserEntity userEntity = userRepository.findByUserName(userDto.getUserName());
        if(userEntity != null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        }

        UserEntity entity = new UserEntity();
        entity.setUserName(userDto.getUserName());
        entity.setFirstName(userDto.getFirstName());
        entity.setLastName(userDto.getLastName());
        entity.setEmail(userDto.getEmail());
        entity.setStatus("A");
        // generate password with Argon2
        String encryptedPassword = argon2Cipher.encrypt(userDto.getPassword());
        entity.setPassword(encryptedPassword);
        userRepository.save(entity);
        // save relationship with user and role
        for(KeyValueDto role : userDto.getRoles()){
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            UserRoleKey userRoleKey = new UserRoleKey();
            userRoleKey.setUserId(entity.getId());
            userRoleKey.setRoleId(role.getId());

            userRoleEntity.setRole(new RoleEntity(role.getId()));
            userRoleEntity.setUser(entity);
            userRoleEntity.setId(userRoleKey);
            userRoleEntity.setCreatedAt(LocalDateTime.now());
            userRoleRepository.save(userRoleEntity);
        }
        //send notification
       // String message = "tú usuario fue creado, tú contraseña es <PASSWORD>".replace("<PASSWORD>", userDto.getPassword());
       // notificationService.sendSms("", userDto.getPhoneNumber(), message, TypeSMS.SMS);
    }


    private CompanyDto getCompanyDto(CompanyEntity entity) {
        CompanyDto companyDto = new CompanyDto();
        companyDto.setId(entity.getId());
        companyDto.setName(entity.getCompanyName());
        companyDto.setDescription(entity.getCompanyDescription());
        companyDto.setAddress(entity.getCompanyAddress());
        companyDto.setRtn(entity.getCompanyRTN());
        companyDto.setType(entity.getType());
        companyDto.setTenantId(entity.getTenantId());
        companyDto.setType(entity.getType());
        // al filtrar por los que no sean globales, esto hace que a fuerzas el isGlobal sea false (se está buscan los roles por empresa)
        Set<KeyValueDto> roles = entity.getCompanyUserEntity().stream()
                                 .filter(f -> !f.getRole().isGlobal())
                                 .map(x -> new KeyValueDto(x.getRole().getId(),
                                                         x.getRole().getRoleName(), x.getRole().getRoleDescription(), false))
                                 .collect(Collectors.toSet());
        companyDto.setRoles( roles);
        companyDto.setActive(entity.getIsActive());
        return companyDto;
    }


}
