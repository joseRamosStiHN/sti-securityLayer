package com.sti.accounting.security_layer.service;

import com.sti.accounting.security_layer.dto.CompanyDto;

import com.sti.accounting.security_layer.dto.CompanyUserDto;
import com.sti.accounting.security_layer.dto.CreateCompanyDto;
import com.sti.accounting.security_layer.dto.KeyValueDto;
import com.sti.accounting.security_layer.entities.*;
import com.sti.accounting.security_layer.repository.*;
import com.sti.accounting.security_layer.utils.CompanyTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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

    public CompanyDto getCompanyById(Long id) {
        log.info("Getting company by id: {}", id);
        CompanyEntity entity = companyRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
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
                    companyUserRole.setUser (user);
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
    public void updateCompany(Long id, CompanyDto companyDto) {
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
                        audit.setActionDate(LocalDateTime.now());
                        companyUserRoleAuditRepository.save(audit);
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
                        audit.setActionDate(LocalDateTime.now());
                        companyUserRoleAuditRepository.save(audit);
                    }
                }
            }
        }

        log.info("Company with id {} updated successfully", id);
    }


    private void responseCompanyDto(CompanyDto dto, CompanyEntity entity) {
        dto.setId(entity.getId());
        dto.setName(entity.getCompanyName());
        dto.setDescription(entity.getCompanyDescription());
        dto.setAddress(entity.getCompanyAddress());
        dto.setPhone(entity.getCompanyPhone());
        dto.setEmail(entity.getCompanyEmail());
        dto.setRtn(entity.getCompanyRTN());
        dto.setType(entity.getType());
        dto.setTenantId(entity.getTenantId());
        dto.setCreatedAt(entity.getCreatedAt().toLocalDate());
        dto.setWebsite(entity.getCompanyWebsite());
        dto.setIsActive(entity.getIsActive());

        if (entity.getCompanyLogo() != null) {
            dto.setCompanyLogo(Base64.getEncoder().encodeToString(entity.getCompanyLogo()));
        }

        // Agregar usuarios y sus roles
        if (entity.getCompanyUserEntity() != null) {
            Map<Long, CompanyUserDto> userMap = new HashMap<>();

            for (CompanyUserRoleEntity userRole : entity.getCompanyUserEntity()) {
                if (!"ACTIVE".equals(userRole.getStatus())) {
                    continue;
                }

                CompanyUserDto userDto = userMap.computeIfAbsent(userRole.getUser().getId(), k -> {
                    CompanyUserDto newUserDto = new CompanyUserDto();
                    newUserDto.setId(userRole.getUser().getId());
                    newUserDto.setRoles(new ArrayList<>());
                    return newUserDto;
                });

                KeyValueDto roleDto = new KeyValueDto();
                roleDto.setId(userRole.getRole().getId());
                roleDto.setName(userRole.getRole().getRoleName());
                userDto.getRoles().add(roleDto);
            }

            dto.setUsers(new ArrayList<>(userMap.values()));
        }
    }


}
