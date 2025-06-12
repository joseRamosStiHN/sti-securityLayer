package com.sti.accounting.security_layer.service;

import com.sti.accounting.security_layer.dto.*;

import com.sti.accounting.security_layer.entities.*;
import com.sti.accounting.security_layer.repository.*;
import com.sti.accounting.security_layer.utils.CompanyTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);

    private final ICompanyRepository companyRepository;
    private final ICompanyUserRepository companyUserRepository;
    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;
    private final ICompanyUserRoleAuditRepository companyUserRoleAuditRepository;


    public CompanyService(ICompanyRepository companyRepository, ICompanyUserRepository companyUserRepository, IRoleRepository roleRepository, IUserRepository userRepository, ICompanyUserRoleAuditRepository companyUserRoleAuditRepository) {
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.companyUserRoleAuditRepository = companyUserRoleAuditRepository;

    }

    public List<CompanyDto> getAllCompany() {
        log.info("Getting all companies");
        return companyRepository.findAll().stream().map(x -> {
            CompanyDto dto = new CompanyDto();
            responseCompanyDto(dto, x);
            return dto;
        }).toList();
    }

    public Page<CompanyByUser> getAllCompanyByUser(Integer page, Integer size, Long userId) {

        Page<CompanyEntity> companyPage = companyRepository.findCompanyByUser(userId, PageRequest.of(page, size));


        List<CompanyByUser> companyDtos = companyPage.getContent().stream().filter(CompanyEntity::getIsActive).map(d -> responseCompanyPaginationDto(d, userId)).toList();

        return new PageImpl<>(companyDtos, PageRequest.of(page, size), companyPage.getTotalElements());
    }

    public CompanyByUser getCompanyByUser(Long userId, Long companyId) {
        CompanyEntity company = companyRepository.getCompanyByIdAndUser(companyId, userId);
        return responseCompanyPaginationDto(company, userId);

    }


    public byte[] getCompanyLogoById(Long id) {
        log.info("Getting company by id: {}", id);

        CompanyEntity entity = companyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("No Company were found with the id %s", id)));

        CompanyDto dto = new CompanyDto();
        responseCompanyDto(dto, entity);
        return entity.getCompanyLogo();

    }


    public CompanyDto getCompanyById(Long id) {
        log.info("Getting company by id: {}", id);
        CompanyEntity entity = companyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                String.format("No Company were found with the id %s", id)));
        CompanyDto dto = new CompanyDto();
        responseCompanyDto(dto, entity);
        return dto;
    }

    @Transactional
    public CompanyDto saveCompany(CompanyDto companyDto) {
        log.info("Saving company: {}", companyDto);

        // 1. Guardar la compañía
        UUID uuid = UUID.randomUUID();
        CompanyEntity company = new CompanyEntity();
        company.setCompanyName(companyDto.getName());
        company.setCompanyDescription(companyDto.getDescription());
        company.setCompanyAddress(companyDto.getAddress());
        company.setCompanyRTN(companyDto.getRtn());
        company.setType(CompanyTypeEnum.valueOf(companyDto.getType().toString()));
        company.setCompanyEmail(companyDto.getEmail());
        company.setCompanyPhone(companyDto.getPhone());
        company.setCompanyWebsite(companyDto.getWebsite());
        company.setIsActive(companyDto.isActive());
        company.setTenantId(uuid.toString());
        company.setCreatedAt(LocalDateTime.now());
        company.setStatus("ACTIVE");

        // Procesar el logo si existe
        if (companyDto.getCompanyLogo() != null && !companyDto.getCompanyLogo().isEmpty()) {
            try {
                byte[] decodedLogo = Base64.getDecoder().decode(companyDto.getCompanyLogo());
                company.setCompanyLogo(decodedLogo);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid logo format. Must be base64 encoded.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Logo file is empty or not provided");
        }

        company = companyRepository.save(company);

        // 2. Procesar usuarios y sus roles
        if (companyDto.getUsers() != null) {
            for (CompanyUserDto userDto : companyDto.getUsers()) {
                UserEntity user = userRepository.findById(userDto.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "User  not found with id: " + userDto.getId()));

                // Crear relaciones para cada rol del usuario
                for (KeyValueDto roleDto : userDto.getRoles()) {
                    RoleEntity role = roleRepository.findById(roleDto.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Role not found with id: " + roleDto.getId()));

                    CompanyUserRoleEntity companyUserRole = new CompanyUserRoleEntity();
                    companyUserRole.setCompany(company);
                    companyUserRole.setUser(user);
                    companyUserRole.setRole(role);
                    companyUserRole.setStatus("ACTIVE");
                    companyUserRole.setCreatedAt(LocalDateTime.now());

                    companyUserRepository.save(companyUserRole);
                }
            }
        }

        log.info("Company with tenantId {} created", uuid);
        CompanyDto savedCompanyDto = new CompanyDto();
        responseCompanyDto(savedCompanyDto, company);
        return savedCompanyDto;
    }

    @Transactional
    public void updateCompany(Long id, Long actionByUser, CompanyDto companyDto) {
        log.info("Updating company with id: {}", id);

        CompanyEntity existingEntity = companyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("No Company were found with the id %s", id)));


        // Actualizar los campos de la compañía
        existingEntity.setCompanyName(companyDto.getName());
        existingEntity.setCompanyDescription(companyDto.getDescription());
        existingEntity.setCompanyAddress(companyDto.getAddress());
        existingEntity.setCompanyRTN(companyDto.getRtn());
        existingEntity.setType(companyDto.getType());
        existingEntity.setCompanyEmail(companyDto.getEmail());
        existingEntity.setCompanyPhone(companyDto.getPhone());
        existingEntity.setCompanyWebsite(companyDto.getWebsite());
        existingEntity.setIsActive(companyDto.isActive());

        // Actualizar el logo si se proporciona uno nuevo
        if (companyDto.getCompanyLogo() != null && !companyDto.getCompanyLogo().isEmpty()) {
            try {
                byte[] decodedLogo = Base64.getDecoder().decode(companyDto.getCompanyLogo());
                existingEntity.setCompanyLogo(decodedLogo);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid logo format. Must be base64 encoded.");
            }
        }

        companyRepository.save(existingEntity);

        // Obtener todas las relaciones existentes
        List<CompanyUserRoleEntity> existingRelations = companyUserRepository.findByCompanyId(id);

        // Procesar usuarios y roles nuevos
        if (companyDto.getUsers() != null) {
            for (CompanyUserDto userDto : companyDto.getUsers()) {
                UserEntity user = userRepository.findById(userDto.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "User not found with id: " + userDto.getId()));

                // Desactivar roles existentes que no están en la nueva lista
                List<CompanyUserRoleEntity> userExistingRelations = existingRelations.stream()
                        .filter(r -> r.getUser().getId().equals(userDto.getId()))
                        .toList();

                for (CompanyUserRoleEntity existingRelation : userExistingRelations) {
                    boolean roleExists = userDto.getRoles().stream()
                            .anyMatch(r -> r.getId().equals(existingRelation.getRole().getId()));

                    if (!roleExists) {
                        // Desactivar en lugar de eliminar
                        existingRelation.setStatus("INACTIVE");
                        companyUserRepository.save(existingRelation);

                        // Registrar en auditoría
                        CompanyUserRoleAuditEntity audit = new CompanyUserRoleAuditEntity();
                        audit.setCompany(existingRelation.getCompany());
                        audit.setUser(existingRelation.getUser());
                        audit.setRole(existingRelation.getRole());
                        audit.setAction("REMOVED");
                        audit.setPreviousStatus("ACTIVE");
                        audit.setNewStatus("INACTIVE");
                        audit.setActionByUser(actionByUser);
                        audit.setActionDate(LocalDateTime.now());
                        companyUserRoleAuditRepository.save(audit);
                    } else {
                        existingRelation.setStatus("ACTIVE");
                        companyUserRepository.save(existingRelation);
                    }
                }

                // Crear o actualizar roles nuevos
                for (KeyValueDto roleDto : userDto.getRoles()) {
                    RoleEntity role = roleRepository.findById(roleDto.getId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Role not found with id: " + roleDto.getId()));

                    CompanyUserRoleEntity existingRelation = existingRelations.stream()
                            .filter(r -> r.getUser().getId().equals(user.getId())
                                    && r.getRole().getId().equals(role.getId()))
                            .findFirst()
                            .orElse(null);

                    if (existingRelation == null) {
                        // Crear nueva relación
                        CompanyUserRoleEntity newRelation = new CompanyUserRoleEntity();
                        newRelation.setCompany(existingEntity);
                        newRelation.setUser(user);
                        newRelation.setRole(role);
                        newRelation.setStatus("ACTIVE");
                        newRelation.setCreatedAt(LocalDateTime.now());
                        companyUserRepository.save(newRelation);

                        // Registrar en auditoría
                        CompanyUserRoleAuditEntity audit = new CompanyUserRoleAuditEntity();
                        audit.setCompany(existingEntity);
                        audit.setUser(user);
                        audit.setRole(role);
                        audit.setAction("ADDED");
                        audit.setNewStatus("ACTIVE");
                        audit.setActionByUser(actionByUser);
                        audit.setActionDate(LocalDateTime.now());
                        companyUserRoleAuditRepository.save(audit);
                    }
                }
            }
        }

        log.info("Company with id {} updated successfully", id);
    }

    @Transactional
    public void deleteCompany(Long id, Long actionByUser) {
        log.info("Deleting company with id: {}", id);

        CompanyEntity company = companyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("No Company were found with the id %s", id)));

        // 1. Eliminar registros de auditoría relacionados
        companyUserRoleAuditRepository.deleteByCompanyId(id);

        // 2. Eliminar relaciones usuario-rol-compañía
        List<CompanyUserRoleEntity> userRoles = companyUserRepository.findByCompanyId(id);
        companyUserRepository.deleteAll(userRoles);

        // 3. Finalmente eliminar la compañía
        companyRepository.delete(company);

        log.info("Company with id {} was permanently deleted", id);
    }

    private void responseCompanyDto(CompanyDto dto, CompanyEntity entity) {
        dto.setId(entity.getId());
        dto.setName(entity.getCompanyName());
        dto.setDescription(entity.getCompanyDescription());
        dto.setAddress(entity.getCompanyAddress());
        dto.setPhone(entity.getCompanyPhone());
        dto.setWebsite(entity.getCompanyWebsite());
        dto.setEmail(entity.getCompanyEmail());
        dto.setRtn(entity.getCompanyRTN());
        dto.setType(entity.getType());
        dto.setTenantId(entity.getTenantId());
        dto.setCreatedAt(entity.getCreatedAt().toLocalDate());
        dto.setIsActive(entity.getIsActive());

        // Agregar usuarios y sus roles
        if (entity.getCompanyUserEntity() != null) {
            Map<Long, CompanyUserDto> userMap = new HashMap<>();

            for (CompanyUserRoleEntity userRole : entity.getCompanyUserEntity()) {
                if (!"ACTIVE".equals(userRole.getStatus())) {
                    continue;
                }

                UserEntity user = userRole.getUser(); // Obtener el usuario

                // Crear o actualizar el CompanyUser Dto
                CompanyUserDto companyUserDto = userMap.computeIfAbsent(user.getId(), k -> {
                    CompanyUserDto newUserDto = new CompanyUserDto();
                    newUserDto.setUser(new UserDto());
                    newUserDto.getUser().setId(user.getId());
                    newUserDto.getUser().setUserName(user.getUserName());
                    newUserDto.getUser().setFirstName(user.getFirstName());
                    newUserDto.getUser().setLastName(user.getLastName());
                    newUserDto.getUser().setEmail(user.getEmail());
                    newUserDto.getUser().setCreatedAt(user.getCreatedAt());
                    newUserDto.getUser().setIsActive(user.getIsActive());
                    newUserDto.setRoles(new ArrayList<>()); // Inicializar la lista de roles
                    newUserDto.getUser().setGlobalRoles(new ArrayList<>()); // Inicializar la lista de globalRoles
                    return newUserDto;
                });

                // Agregar el rol al usuario
                KeyValueDto roleDto = new KeyValueDto();
                roleDto.setId(userRole.getRole().getId());
                roleDto.setName(userRole.getRole().getRoleName());
                roleDto.setDescription(userRole.getRole().getRoleDescription());
                roleDto.setGlobal(userRole.getRole().getIsGlobal());

                companyUserDto.getRoles().add(roleDto);

                // Obtener los roles globales del usuario
                Set<RoleEntity> globalRoles = user.getGlobalRoles();
                List<KeyValueDto> globalRolesDto = globalRoles.stream()
                        .map(role -> {
                            KeyValueDto globalRoleDto = new KeyValueDto();
                            globalRoleDto.setId(role.getId());
                            globalRoleDto.setName(role.getRoleName());
                            globalRoleDto.setDescription(role.getRoleDescription());
                            globalRoleDto.setGlobal(role.getIsGlobal());
                            return globalRoleDto;
                        })
                        .toList();

                companyUserDto.getUser().setGlobalRoles(globalRolesDto);
            }

            dto.setUsers(new ArrayList<>(userMap.values()));
        }
    }


    private CompanyByUser responseCompanyPaginationDto(CompanyEntity entity, Long userId) {

        CompanyByUser dto = new CompanyByUser();
        dto.setId(entity.getId());
        dto.setName(entity.getCompanyName());
        dto.setDescription(entity.getCompanyDescription());
        dto.setAddress(entity.getCompanyAddress());
        dto.setPhone(entity.getCompanyPhone());
        dto.setWebsite(entity.getCompanyWebsite());
        dto.setEmail(entity.getCompanyEmail());
        dto.setRtn(entity.getCompanyRTN());
        dto.setType(entity.getType());
        dto.setTenantId(entity.getTenantId());
        dto.setCreatedAt(entity.getCreatedAt().toLocalDate());
        dto.setRoles(getRolesByCompanies(entity.getId(), userId));

        return dto;

    }

    private List<KeyValueDto> getRolesByCompanies(Long companyId, Long userId) {
        return roleRepository.getRoleByCompany(companyId, userId).stream().map(r -> {
            KeyValueDto keyValueDto = new KeyValueDto();
            keyValueDto.setId(r.getId());
            keyValueDto.setName(r.getRoleName());
            keyValueDto.setDescription(r.getRoleDescription());
            keyValueDto.setGlobal(r.getIsGlobal());
            return keyValueDto;
        }).toList();
    }
}
