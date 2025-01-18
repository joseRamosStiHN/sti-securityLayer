package com.sti.accounting.security_layer.service;

import com.sti.accounting.security_layer.core.Argon2Cipher;
import com.sti.accounting.security_layer.dto.CompanyDto;
import com.sti.accounting.security_layer.dto.CreateUserDto;
import com.sti.accounting.security_layer.dto.KeyValueDto;
import com.sti.accounting.security_layer.dto.UserDto;
import com.sti.accounting.security_layer.entities.*;
import com.sti.accounting.security_layer.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;
    private final Argon2Cipher argon2Cipher;
    private final NotificationService notificationService;
    private final ICompanyUserRepository companyUserRoleRepository;
    private final ICompanyRepository companyRepository;
    private final IRoleRepository roleRepository;
    private final IPermissionRepository permissionRepository;

    public UserService(IUserRepository userRepository, IUserRoleRepository userRoleRepository, Argon2Cipher argon2Cipher, NotificationService notificationService, ICompanyUserRepository companyUserRoleRepository, ICompanyRepository companyRepository, IRoleRepository roleRepository, IPermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.argon2Cipher = argon2Cipher;
        this.notificationService = notificationService;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }


    public List<UserDto> getAllUsers() {
        log.info("Get all users");
        return userRepository.findAll().stream().map(x -> {
            UserDto dto = new UserDto();
            dto.setId(x.getId());
            dto.setUserName(x.getUserName());
            dto.setFirstName(x.getFirstName());
            dto.setLastName(x.getLastName());
            dto.setEmail(x.getEmail());
            dto.setCreatedAt(x.getCreatedAt().toLocalDate());
            dto.setRoles(x.getUserRoles().stream()
                    .map(role -> new KeyValueDto(role.getRole().getId(), role.getRole().getRoleName(), role.getRole().getRoleDescription(), role.getRole().getIsGlobal()))
                    .toList());

            // Transformar Set<CompanyUser RoleEntity> a List<CompanyDto>
            dto.setCompanies(x.getCompanyUser().stream()
                    .map(companyUserRole -> {
                        CompanyDto companyDto = new CompanyDto();
                        companyDto.setId(companyUserRole.getCompany().getId());
                        companyDto.setName(companyUserRole.getCompany().getCompanyName());
                        companyDto.setDescription(companyUserRole.getCompany().getCompanyDescription());
                        companyDto.setAddress(companyUserRole.getCompany().getCompanyAddress());
                        companyDto.setRtn(companyUserRole.getCompany().getCompanyRTN());
                        companyDto.setType(companyUserRole.getCompany().getType());
                        companyDto.setTenantId(companyUserRole.getCompany().getTenantId());
                        companyDto.setIsActive(companyUserRole.getCompany().getIsActive());
                        return companyDto;
                    })
                    .toList());

            return dto;
        }).toList();
    }

    public UserDto getUserByUserNameAndPassword(String userName, String password) {
        log.info("Get user by username {}", userName);
        UserEntity entity = userRepository.findByUserName(userName);

        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User or Password not match");
        }

        if (!argon2Cipher.matches(password, entity.getPassword())) {
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
                .filter(f -> f.getRole().getIsGlobal())
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
        UserEntity entity = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No User were found with the id %s", id)));
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setUserName(entity.getUserName());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setEmail(entity.getEmail());
        dto.setCreatedAt(entity.getCreatedAt().toLocalDate());
        // al filtrar que solo sean los globales, esto hace que a fuerzas el isGlobal sea true
        List<KeyValueDto> globalRoles = entity.getUserRoles().stream()
                .filter(f -> f.getRole().getIsGlobal())
                .map(x -> new KeyValueDto(x.getRole().getId(),
                        x.getRole().getRoleName(), x.getRole().getRoleDescription(), true)).toList();
        dto.setRoles(globalRoles);
        dto.setActive(entity.getStatus().equalsIgnoreCase("A"));
        //set companies
        List<CompanyDto> companyDtoList = entity.getCompanyUser().stream().map(x -> getCompanyDto(x.getCompany())).toList();
        dto.setCompanies(companyDtoList);

        return dto;
    }

//    public void createUser(CreateUserDto userDto) {
//        log.info("Create user");
//
//        // check if user already exists
//        UserEntity userEntity = userRepository.findByUserName(userDto.getUserName());
//        if (userEntity != null) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
//        }
//
//        // Create and save user
//        UserEntity entity = new UserEntity();
//        entity.setUserName(userDto.getUserName());
//        entity.setFirstName(userDto.getFirstName());
//        entity.setLastName(userDto.getLastName());
//        entity.setEmail(userDto.getEmail());
//        entity.setStatus("A");
//        entity.setPassword(argon2Cipher.encrypt(userDto.getPassword()));
//        entity.setCreatedAt(LocalDateTime.now());
//        userRepository.save(entity);
//
//        // Save user roles
//        for (KeyValueDto roleDto : userDto.getRoles()) {
//            RoleEntity roleEntity = roleRepository.findById(roleDto.getId())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));
//
//            UserRoleEntity userRoleEntity = new UserRoleEntity();
//            UserRoleKey userRoleKey = new UserRoleKey();
//            userRoleKey.setUserId(entity.getId());
//            userRoleKey.setRoleId(roleEntity.getId());
//
//            userRoleEntity.setRole(roleEntity);
//            userRoleEntity.setUser(entity);
//            userRoleEntity.setId(userRoleKey);
//            userRoleEntity.setCreatedAt(LocalDateTime.now());
//            userRoleRepository.save(userRoleEntity);
//        }
//
//        // Save company user roles
//        if (userDto.getCompanies() != null) {
//            for (CompanyDto companyDto : userDto.getCompanies()) {
//                CompanyEntity companyEntity = companyRepository.findById(companyDto.getId())
//                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));
//
//                if (companyDto.getRoles() != null) {
//                    for (KeyValueDto roleDto : companyDto.getRoles()) {
//                        RoleEntity roleEntity = roleRepository.findById(roleDto.getId())
//                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));
//
//                        CompanyUserRoleEntity companyUserRoleEntity = new CompanyUserRoleEntity();
//                        companyUserRoleEntity.setCompany(companyEntity);
//                        companyUserRoleEntity.setUser(entity);
//                        companyUserRoleEntity.setRole(roleEntity);
//                        companyUserRoleEntity.setStatus("ACTIVE");
//                        companyUserRoleEntity.setCreatedAt(LocalDateTime.now());
//
//                        // Set permissions if they exist
//                        if (companyDto.getPermissions() != null) {
//                            for (Long permissionId : companyDto.getPermissions()) {
//                                PermissionsEntity permissionEntity = permissionRepository.findById(permissionId)
//                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission not found"));
//                                companyUserRoleEntity.setPermissions(permissionEntity);
//                            }
//                        }
//
//                        companyUserRoleRepository.save(companyUserRoleEntity);
//                    }
//                }
//            }
//        }
//
//    }

//    @Transactional
//    public void updateUser(Long id, CreateUserDto userDto) {
//        log.info("Update user with id {}", id);
//
//        UserEntity existingUser = userRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
//
//        // Actualizar los campos del usuario
//        existingUser.setUserName(userDto.getUserName());
//        existingUser.setFirstName(userDto.getFirstName());
//        existingUser.setLastName(userDto.getLastName());
//        existingUser.setEmail(userDto.getEmail());
//
//        // Si se proporciona una nueva contraseña, actualizarla
//        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
//            existingUser.setPassword(argon2Cipher.encrypt(userDto.getPassword()));
//        }
//
//        userRepository.save(existingUser);
//
//        // Actualizar roles del usuario
//        // Obtener roles existentes
//        Set<Long> existingRoleIds = existingUser.getUserRoles().stream()
//                .map(role -> role.getRole().getId())
//                .collect(Collectors.toSet());
//
//        // Obtener nuevos roles
//        Set<Long> newRoleIds = userDto.getRoles().stream()
//                .map(KeyValueDto::getId)
//                .collect(Collectors.toSet());
//
//        // Eliminar roles que ya no están presentes
//        existingRoleIds.stream()
//                .filter(roleId -> !newRoleIds.contains(roleId))
//                .forEach(roleId -> {
//                    userRoleRepository.deleteByUserIdAndRoleId(id, roleId);
//                });
//
//        // Agregar nuevos roles
//        for (KeyValueDto roleDto : userDto.getRoles()) {
//            if (!existingRoleIds.contains(roleDto.getId())) {
//                RoleEntity roleEntity = roleRepository.findById(roleDto.getId())
//                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));
//
//                UserRoleEntity userRoleEntity = new UserRoleEntity();
//                UserRoleKey userRoleKey = new UserRoleKey();
//                userRoleKey.setUserId(existingUser.getId());
//                userRoleKey.setRoleId(roleEntity.getId());
//
//                userRoleEntity.setRole(roleEntity);
//                userRoleEntity.setUser(existingUser);
//                userRoleEntity.setId(userRoleKey);
//                userRoleEntity.setCreatedAt(LocalDateTime.now());
//                userRoleRepository.save(userRoleEntity);
//            }
//        }
//
//        // Actualizar roles de la empresa
//        if (userDto.getCompanies() != null) {
//            for (CompanyDto companyDto : userDto.getCompanies()) {
//                CompanyEntity companyEntity = companyRepository.findById(companyDto.getId())
//                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));
//
//                // Obtener roles existentes para la empresa
//                Set<Long> existingCompanyRoleIds = existingUser.getCompanyUser().stream()
//                        .filter(companyUserRole -> companyUserRole.getCompany().getId().equals(companyDto.getId()))
//                        .map(companyUserRole -> companyUserRole.getRole().getId())
//                        .collect(Collectors.toSet());
//
//                // Obtener nuevos roles para la empresa
//                Set<Long> newCompanyRoleIds = companyDto.getRoles().stream()
//                        .map(KeyValueDto::getId)
//                        .collect(Collectors.toSet());
//
//                // Eliminar roles de empresa que ya no están presentes
//                existingCompanyRoleIds.stream()
//                        .filter(roleId -> !newCompanyRoleIds.contains(roleId))
//                        .forEach(roleId -> {
//                            companyUserRoleRepository.deleteByUserIdAndCompanyIdAndRoleId(existingUser.getId(), companyDto.getId(), roleId);
//                        });
//
//                // Agregar nuevos roles de empresa
//                for (KeyValueDto roleDto : companyDto.getRoles()) {
//                    if (!existingCompanyRoleIds.contains(roleDto.getId())) {
//                        RoleEntity roleEntity = roleRepository.findById(roleDto.getId())
//                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));
//
//                        CompanyUserRoleEntity companyUserRoleEntity = new CompanyUserRoleEntity();
//                        companyUserRoleEntity.setCompany(companyEntity);
//                        companyUserRoleEntity.setUser(existingUser);
//                        companyUserRoleEntity.setRole(roleEntity);
//                        companyUserRoleEntity.setStatus("ACTIVE");
//                        companyUserRoleEntity.setCreatedAt(LocalDateTime.now());
//
//                        // Set permissions if they exist
//                        if (companyDto.getPermissions() != null) {
//                            for (Long permissionId : companyDto.getPermissions()) {
//                                PermissionsEntity permissionEntity = permissionRepository.findById(permissionId)
//                                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission not found"));
//                                companyUserRoleEntity.setPermissions(permissionEntity);
//                            }
//                        }
//
//                        companyUserRoleRepository.save(companyUserRoleEntity);
//                    }
//                }
//            }
//        }
//    }

    private CompanyDto getCompanyDto(CompanyEntity entity) {
        CompanyDto companyDto = new CompanyDto();
        companyDto.setId(entity.getId());
        companyDto.setName(entity.getCompanyName());
        companyDto.setDescription(entity.getCompanyDescription());
        companyDto.setAddress(entity.getCompanyAddress());
        companyDto.setRtn(entity.getCompanyRTN());
        companyDto.setType(entity.getType());
        companyDto.setEmail(entity.getCompanyEmail());
        companyDto.setPhone(entity.getCompanyPhone());
        companyDto.setWebsite(entity.getCompanyWebsite());
        companyDto.setTenantId(entity.getTenantId());
        companyDto.setIsActive(entity.getIsActive());

//        // Obtener roles de la compañía
//        Set<KeyValueDto> roles = entity.getCompanyUserEntity().stream()
//                .map(x -> new KeyValueDto(x.getRole().getId(),
//                        x.getRole().getRoleName(), x.getRole().getRoleDescription(), x.getRole().getIsGlobal()))
//                .collect(Collectors.toSet());
//        companyDto.setRoles(roles);
//
//        // Obtener permisos de la compañía
//        List<Long> permissionIds = entity.getCompanyUserEntity().stream()
//                .map(companyUserRole -> companyUserRole.getPermissions() != null ? companyUserRole.getPermissions().getId() : null)
//            .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//        companyDto.setPermissions(permissionIds);

        return companyDto;
    }

}
