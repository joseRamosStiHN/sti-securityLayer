package com.sti.accounting.security_layer.service;

import com.sti.accounting.security_layer.core.Argon2Cipher;
import com.sti.accounting.security_layer.dto.*;
import com.sti.accounting.security_layer.entities.*;
import com.sti.accounting.security_layer.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

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
    private final ICompanyUserRoleAuditRepository companyUserRoleAuditRepository;

    public UserService(IUserRepository userRepository, IUserRoleRepository userRoleRepository, Argon2Cipher argon2Cipher, NotificationService notificationService, ICompanyUserRepository companyUserRoleRepository, ICompanyRepository companyRepository, IRoleRepository roleRepository, ICompanyUserRoleAuditRepository companyUserRoleAuditRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.argon2Cipher = argon2Cipher;
        this.notificationService = notificationService;
        this.companyUserRoleRepository = companyUserRoleRepository;
        this.companyRepository = companyRepository;
        this.roleRepository = roleRepository;
        this.companyUserRoleAuditRepository = companyUserRoleAuditRepository;
    }


    public List<UserDto> getAllUsers() {
        log.info("Get all users");
        return userRepository.findAll().stream()
                .map(this::convertToUserDto)
                .toList();
    }


    public UserDto getUserById(Long id) {
        log.info("Get user by id {}", id);
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No User were found with the id %s", id)));
        return convertToUserDto(entity);
    }

    public UserDto getUserByUserNameAndPassword(String userName, String password) {
        log.info("Get user by username {}", userName);
        UserEntity entity = userRepository.findByUserName(userName);

        if (entity == null || !argon2Cipher.matches(password, entity.getPassword())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User  or Password not match");
        }

        return convertToUserDtoLogin(entity);
    }


    public void createUser(CreateUserDto userDto) {
        log.info("Create user");

        // Verificar si el usuario ya existe
        if (userRepository.findByUserName(userDto.getUserName()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        }

        // Crear y guardar el usuario
        UserEntity entity = new UserEntity();
        entity.setUserName(userDto.getUserName());
        entity.setFirstName(userDto.getFirstName());
        entity.setLastName(userDto.getLastName());
        entity.setEmail(userDto.getEmail());
        entity.setUserAddress(userDto.getUserAddress());
        entity.setUserPhone(userDto.getUserPhone());
        entity.setIsActive(userDto.isActive());
        entity.setPassword(argon2Cipher.encrypt(userDto.getPassword()));
        entity.setCreatedAt(LocalDateTime.now());

        userRepository.save(entity);
        // Guardar roles globales
        saveUserRoles(userDto.getGlobalRoles(), entity);

        // Guardar roles de usuario en compañías
        if (userDto.getCompanies() != null) {
            for (CompanyUserDto companyUserDto : userDto.getCompanies()) {
                CompanyEntity companyEntity = companyRepository.findById(companyUserDto.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No Company were found with the id %s", companyUserDto.getId())));

                saveCompanyUserRoles(companyUserDto.getRoles(), entity, companyEntity);
            }
        }


    }

    @Transactional
    public void updateUser(Long id, Long actionByUser, CreateUserDto userDto) {
        log.info("Update user with id {}", id);

        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User  not found"));

        // Update basic user information
        existingUser.setUserName(userDto.getUserName());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setUserAddress(userDto.getUserAddress());
        existingUser.setUserPhone(userDto.getUserPhone());
        existingUser.setIsActive(userDto.isActive());

        // Update password if provided
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(argon2Cipher.encrypt(userDto.getPassword()));
        }

        userRepository.save(existingUser);

        // Obtener la empresa asociada al usuario
        List<CompanyUserRoleEntity> userCompanyRoles = companyUserRoleRepository.findByUserId(existingUser.getId());
        if (userCompanyRoles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No companies found for user");
        }

        // Suponiendo que solo necesitas la primera empresa
        CompanyEntity company = userCompanyRoles.get(0).getCompany();

        // Update global roles
        List<UserRoleEntity> existingGlobalRoles = userRoleRepository.findByUserId(id);

        // Deactivate global roles that are not in the new list
        for (UserRoleEntity existingRole : existingGlobalRoles) {
            boolean roleExists = userDto.getGlobalRoles().stream()
                    .anyMatch(r -> r.getId().equals(existingRole.getRole().getId()));

            if (!roleExists) {
                // Create audit record
                CompanyUserRoleAuditEntity audit = new CompanyUserRoleAuditEntity();
                audit.setCompany(company);
                audit.setUser(existingUser);
                audit.setRole(existingRole.getRole());
                audit.setAction("REMOVED");
                audit.setPreviousStatus("ACTIVE");
                audit.setNewStatus("INACTIVE");
                audit.setActionByUser(actionByUser);
                audit.setActionDate(LocalDateTime.now());
                companyUserRoleAuditRepository.save(audit);

                // Remove the role
                userRoleRepository.delete(existingRole);
            }
        }

        // Crear o actualizar roles nuevos
        for (KeyValueDto roleDto : userDto.getGlobalRoles()) {
            boolean roleExists = existingGlobalRoles.stream()
                    .anyMatch(r -> r.getRole().getId().equals(roleDto.getId()));

            if (!roleExists) {
                RoleEntity roleEntity = roleRepository.findById(roleDto.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Role not found with id: " + roleDto.getId()));

                UserRoleEntity userRoleEntity = new UserRoleEntity();
                UserRoleKey userRoleKey = new UserRoleKey();
                userRoleKey.setUserId(existingUser.getId());
                userRoleKey.setRoleId(roleEntity.getId());

                userRoleEntity.setRole(roleEntity);
                userRoleEntity.setUser(existingUser);
                userRoleEntity.setId(userRoleKey);
                userRoleEntity.setCreatedAt(LocalDateTime.now());
                userRoleRepository.save(userRoleEntity);

                // Create audit record
                CompanyUserRoleAuditEntity audit = new CompanyUserRoleAuditEntity();
                audit.setCompany(company); // Aquí se asigna la empresa
                audit.setUser(existingUser);
                audit.setRole(roleEntity);
                audit.setAction("ADDED");
                audit.setNewStatus("ACTIVE");
                audit.setActionByUser(actionByUser);
                audit.setActionDate(LocalDateTime.now());
                companyUserRoleAuditRepository.save(audit);
            }
        }

        // Update company roles

        if (userDto.getCompanies() != null) {
            for (CompanyUserDto companyUserDto : userDto.getCompanies()) {
                CompanyEntity companyEntity = companyRepository.findById(companyUserDto.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Company not found with id: " + companyUserDto.getId()));

                List<CompanyUserRoleEntity> existingCompanyRoles =
                        companyUserRoleRepository.findByCompanyIdAndUserId(companyEntity.getId(), existingUser.getId());

                // Desactivar todos los roles existentes si se envía un array vacío
                if (companyUserDto.getRoles() == null || companyUserDto.getRoles().isEmpty()) {
                    for (CompanyUserRoleEntity existingRole : existingCompanyRoles) {
                        if ("ACTIVE".equals(existingRole.getStatus())) {
                            existingRole.setStatus("INACTIVE");
                            companyUserRoleRepository.save(existingRole);

                            // Create audit record
                            CompanyUserRoleAuditEntity audit = new CompanyUserRoleAuditEntity();
                            audit.setCompany(companyEntity);
                            audit.setUser(existingUser);
                            audit.setRole(existingRole.getRole());
                            audit.setAction("REMOVED");
                            audit.setPreviousStatus("ACTIVE");
                            audit.setNewStatus("INACTIVE");
                            audit.setActionByUser(actionByUser);
                            audit.setActionDate(LocalDateTime.now());
                            companyUserRoleAuditRepository.save(audit);
                        }
                    }
                    continue;
                }

                // Deactivate company roles that are not in the new list
                for (CompanyUserRoleEntity existingRole : existingCompanyRoles) {
                    if (!"ACTIVE".equals(existingRole.getStatus())) {
                        continue;
                    }

                    boolean roleExists = companyUserDto.getRoles().stream()
                            .anyMatch(r -> r.getId().equals(existingRole.getRole().getId()));

                    if (!roleExists) {
                        existingRole.setStatus("INACTIVE");
                        companyUserRoleRepository.save(existingRole);

                        // Create audit record
                        CompanyUserRoleAuditEntity audit = new CompanyUserRoleAuditEntity();
                        audit.setCompany(companyEntity);
                        audit.setUser(existingUser);
                        audit.setRole(existingRole.getRole());
                        audit.setAction("REMOVED");
                        audit.setPreviousStatus("ACTIVE");
                        audit.setNewStatus("INACTIVE");
                        audit.setActionByUser(actionByUser);
                        audit.setActionDate(LocalDateTime.now());
                        companyUserRoleAuditRepository.save(audit);
                    }
                }

                // Add new company roles
                for (KeyValueDto roleDto : companyUserDto.getRoles()) {
                    boolean roleExists = existingCompanyRoles.stream()
                            .anyMatch(r -> r.getRole().getId().equals(roleDto.getId()) &&
                                    "ACTIVE".equals(r.getStatus()));

                    if (!roleExists) {
                        RoleEntity roleEntity = roleRepository.findById(roleDto.getId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Role not found with id: " + roleDto.getId()));

                        CompanyUserRoleEntity companyUserRole = new CompanyUserRoleEntity();
                        companyUserRole.setCompany(companyEntity);
                        companyUserRole.setUser(existingUser);
                        companyUserRole.setRole(roleEntity);
                        companyUserRole.setStatus("ACTIVE");
                        companyUserRole.setCreatedAt(LocalDateTime.now());
                        companyUserRoleRepository.save(companyUserRole);

                        // Create audit record
                        CompanyUserRoleAuditEntity audit = new CompanyUserRoleAuditEntity();
                        audit.setCompany(companyEntity);
                        audit.setUser(existingUser);
                        audit.setRole(roleEntity);
                        audit.setAction("ADDED");
                        audit.setNewStatus("ACTIVE");
                        audit.setActionByUser(actionByUser);
                        audit.setActionDate(LocalDateTime.now());
                        companyUserRoleAuditRepository.save(audit);
                    }
                }
            }
        }

        log.info("User  with id {} updated successfully", id);
    }

    private UserDto convertToUserDto(UserEntity entity) {
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setUserName(entity.getUserName());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setUserAddress(entity.getUserAddress());
        dto.setUserPhone(entity.getUserPhone());
        dto.setIsActive(entity.getIsActive());

        // Set global roles
        List<KeyValueDto> globalRoles = new ArrayList<>(entity.getUserRoles().stream()
                .filter(f -> f.getRole().getIsGlobal())
                .map(x -> new KeyValueDto(
                        x.getRole().getId(),
                        x.getRole().getRoleName(),
                        x.getRole().getRoleDescription(),
                        true
                ))
                .toList());
        dto.setGlobalRoles(globalRoles);

        // Mapa para agrupar compañías por ID
        Map<Long, CompanyUserDto> companyUserMap = new HashMap<>();

        // Primero, agregar todas las compañías del usuario
        for (CompanyUserRoleEntity companyUserRole : entity.getCompanyUser()) {
            CompanyEntity company = companyUserRole.getCompany();
            Long companyId = company.getId();

            // Crear o obtener el CompanyUser Dto solo si no existe
            companyUserMap.computeIfAbsent(companyId, k -> {
                CompanyUserDto newCompanyUserDto = new CompanyUserDto();
                newCompanyUserDto.setId(companyUserRole.getId());
                newCompanyUserDto.setUser(null);
                newCompanyUserDto.setRoles(new ArrayList<>());

                // Crear el CompanyDto
                CompanyDto companyDto = new CompanyDto();
                companyDto.setId(companyId);
                companyDto.setName(company.getCompanyName());
                companyDto.setDescription(company.getCompanyDescription());
                companyDto.setAddress(company.getCompanyAddress());
                companyDto.setRtn(company.getCompanyRTN());
                companyDto.setType(company.getType());
                companyDto.setEmail(company.getCompanyEmail());
                companyDto.setPhone(company.getCompanyPhone());
                companyDto.setWebsite(company.getCompanyWebsite());
                companyDto.setTenantId(company.getTenantId());
                companyDto.setCompanyLogo(Base64.getEncoder().encodeToString(company.getCompanyLogo()));
                companyDto.setIsActive(true);

                newCompanyUserDto.setCompany(companyDto);
                return newCompanyUserDto;
            });

            // Agregar roles activos a la compañía
            if ("ACTIVE".equals(companyUserRole.getStatus())) {
                KeyValueDto roleDto = new KeyValueDto(
                        companyUserRole.getRole().getId(),
                        companyUserRole.getRole().getRoleName(),
                        companyUserRole.getRole().getRoleDescription(),
                        false
                );
                companyUserMap.get(companyId).getRoles().add(roleDto);
            }
        }

        // Asignar la lista de CompanyUser Dto al DTO de usuario
        dto.setCompanies(new ArrayList<>(companyUserMap.values()));
        return dto;
    }

    private UserDto convertToUserDtoLogin(UserEntity entity) {
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setUserName(entity.getUserName());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmail(entity.getEmail());
        dto.setUserAddress(entity.getUserAddress());
        dto.setUserPhone(entity.getUserPhone());
        dto.setIsActive(entity.getIsActive());

        // Set global roles
        List<KeyValueDto> globalRoles = new ArrayList<>(entity.getUserRoles().stream()
                .filter(f -> f.getRole().getIsGlobal())
                .map(x -> new KeyValueDto(
                        x.getRole().getId(),
                        x.getRole().getRoleName(),
                        x.getRole().getRoleDescription(),
                        true
                ))
                .toList());
        dto.setGlobalRoles(globalRoles);

        return dto;
    }

    private void saveUserRoles(List<KeyValueDto> roles, UserEntity user) {
        if (roles != null) {
            for (KeyValueDto roleDto : roles) {
                RoleEntity roleEntity = roleRepository.findById(roleDto.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found with id: " + roleDto.getId()));

                UserRoleEntity userRoleEntity = new UserRoleEntity();
                UserRoleKey userRoleKey = new UserRoleKey();
                userRoleKey.setUserId(user.getId());
                userRoleKey.setRoleId(roleEntity.getId());

                userRoleEntity.setRole(roleEntity);
                userRoleEntity.setUser(user);
                userRoleEntity.setId(userRoleKey);
                userRoleEntity.setCreatedAt(LocalDateTime.now());
                userRoleRepository.save(userRoleEntity);
            }
        }
    }

    private void saveCompanyUserRoles(List<KeyValueDto> roles, UserEntity user, CompanyEntity company) {
        if (roles != null) {
            for (KeyValueDto roleDto : roles) {
                RoleEntity roleEntity = roleRepository.findById(roleDto.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found with id: " + roleDto.getId()));

                CompanyUserRoleEntity companyUserRoleEntity = new CompanyUserRoleEntity();
                companyUserRoleEntity.setCompany(company);
                companyUserRoleEntity.setUser(user);
                companyUserRoleEntity.setRole(roleEntity);
                companyUserRoleEntity.setStatus("ACTIVE");
                companyUserRoleEntity.setCreatedAt(LocalDateTime.now());
                companyUserRoleRepository.save(companyUserRoleEntity);
            }
        }
    }


}
