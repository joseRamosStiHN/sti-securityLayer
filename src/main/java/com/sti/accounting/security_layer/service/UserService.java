package com.sti.accounting.security_layer.service;

import com.sti.accounting.security_layer.core.Argon2Cipher;
import com.sti.accounting.security_layer.dto.*;
import com.sti.accounting.security_layer.entities.*;
import com.sti.accounting.security_layer.repository.*;
import com.sti.accounting.security_layer.utils.PasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public List<UserDto> getUsersByComapany(Long id) {
        log.info("Get user by id {}", id);
        return userRepository.getUsersByCompany(id).stream().map(this::convertToUserDto).toList();

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
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 1. Actualizar información básica del usuario
        updateBasicInfo(user, userDto);

        // 2. Actualizar roles globales (sin depender de compañías)
        updateRoles(user, userDto.getGlobalRoles(), actionByUser);

        // 3. Actualizar roles por compañía (maneja caso cuando no hay compañías)
        if (userDto.getCompanies() != null) {
            userDto.getCompanies().forEach(companyDto ->
                    updateCompanyRoles(user, companyDto, actionByUser)
            );
        }
    }

    private void updateBasicInfo(UserEntity user, CreateUserDto dto) {
        user.setUserName(dto.getUserName());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setUserAddress(dto.getUserAddress());
        user.setUserPhone(dto.getUserPhone());
        user.setIsActive(dto.isActive());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(argon2Cipher.encrypt(dto.getPassword()));
        }
        userRepository.save(user);
    }

    private void updateRoles(UserEntity user, List<KeyValueDto> newRoles, Long actionByUser) {
        if (newRoles == null) return;

        List<UserRoleEntity> currentRoles = userRoleRepository.findByUserId(user.getId());

        // Eliminar roles que ya no están
        currentRoles.stream()
                .filter(role -> newRoles.stream().noneMatch(r -> r.getId().equals(role.getRole().getId())))
                .forEach(role -> {
                    userRoleRepository.delete(role);
                    logRoleChange(null, user, role.getRole(), "REMOVED", actionByUser);
                });

        // Agregar nuevos roles
        newRoles.stream()
                .filter(roleDto -> currentRoles.stream().noneMatch(r -> r.getRole().getId().equals(roleDto.getId())))
                .forEach(roleDto -> {
                    RoleEntity role = roleRepository.findById(roleDto.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));

                    UserRoleEntity userRole = new UserRoleEntity();
                    userRole.setId(new UserRoleKey(user.getId(), role.getId()));
                    userRole.setUser(user);
                    userRole.setRole(role);
                    userRoleRepository.save(userRole);

                    logRoleChange(null, user, role, "ADDED", actionByUser);
                });
    }

    private void updateCompanyRoles(UserEntity user, CompanyUserDto companyDto, Long actionByUser) {
        CompanyEntity company = companyRepository.findById(companyDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));

        List<CompanyUserRoleEntity> currentRoles = companyUserRoleRepository
                .findByCompanyIdAndUserId(company.getId(), user.getId());

        // Manejar eliminación de todos los roles
        if (companyDto.getRoles() == null || companyDto.getRoles().isEmpty()) {
            currentRoles.forEach(role -> {
                role.setStatus("INACTIVE");
                companyUserRoleRepository.save(role);
                logRoleChange(company, user, role.getRole(), "REMOVED", actionByUser);
            });
            return;
        }

        // Sincronizar roles
        syncRoles(user, company, currentRoles, companyDto.getRoles(), actionByUser);
    }

    private void syncRoles(UserEntity user, CompanyEntity company,
                           List<CompanyUserRoleEntity> currentRoles,
                           List<KeyValueDto> newRoles, Long actionByUser) {
        // Desactivar roles eliminados
        currentRoles.stream()
                .filter(role -> "ACTIVE".equals(role.getStatus()))
                .filter(role -> newRoles.stream().noneMatch(r -> r.getId().equals(role.getRole().getId())))
                .forEach(role -> {
                    role.setStatus("INACTIVE");
                    companyUserRoleRepository.save(role);
                    logRoleChange(company, user, role.getRole(), "REMOVED", actionByUser);
                });

        // Agregar nuevos roles
        newRoles.stream()
                .filter(roleDto -> currentRoles.stream()
                        .noneMatch(r -> r.getRole().getId().equals(roleDto.getId()) && "ACTIVE".equals(r.getStatus())))
                .forEach(roleDto -> {
                    RoleEntity role = roleRepository.findById(roleDto.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado"));

                    CompanyUserRoleEntity newRole = new CompanyUserRoleEntity();
                    newRole.setCompany(company);
                    newRole.setUser(user);
                    newRole.setRole(role);
                    newRole.setStatus("ACTIVE");
                    companyUserRoleRepository.save(newRole);

                    logRoleChange(company, user, role, "ADDED", actionByUser);
                });
    }

    private void logRoleChange(CompanyEntity company, UserEntity user,
                               RoleEntity role, String action, Long actionByUser) {
        if (company == null) return;

        CompanyUserRoleAuditEntity audit = new CompanyUserRoleAuditEntity();
        audit.setCompany(company);
        audit.setUser(user);
        audit.setRole(role);
        audit.setAction(action);
        audit.setActionByUser(actionByUser);
        audit.setActionDate(LocalDateTime.now());

        if ("REMOVED".equals(action)) {
            audit.setPreviousStatus("ACTIVE");
            audit.setNewStatus("INACTIVE");
        } else {
            audit.setNewStatus("ACTIVE");
        }

        companyUserRoleAuditRepository.save(audit);
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
        dto.setCreatedAt(entity.getCreatedAt());
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

    @Transactional
    public void recoverPassword(PasswordRecoveryRequest request) {

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with this email not found"));

        PasswordGenerator generator = new PasswordGenerator();

        String newPassword = generator.generatePassword(8);

        user.setPassword(argon2Cipher.encrypt(newPassword));
        userRepository.save(user);

        try {
            String emailContent = notificationService.buildRecoveryEmail(user.getFirstName(), newPassword);
            notificationService.sendEmail("lcaceres@stiglobals.com", user.getEmail(), "Recuperación de Contraseña", emailContent);
        } catch (Exception e) {
            log.error("Error sending recovery email", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending recovery email");
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password and confirmation do not match");
        }

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!argon2Cipher.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }

        if (argon2Cipher.matches(request.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from current password");
        }

        user.setPassword(argon2Cipher.encrypt(request.getNewPassword()));
        userRepository.save(user);
    }

}
