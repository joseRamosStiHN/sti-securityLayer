package com.sti.accounting.securityLayer.service;

import com.sti.accounting.securityLayer.dto.CompanyDto;

import com.sti.accounting.securityLayer.dto.KeyValueDto;
import com.sti.accounting.securityLayer.entities.*;
import com.sti.accounting.securityLayer.repository.*;
import com.sti.accounting.securityLayer.utils.CompanyTypeEnum;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CompanyService {


    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);
    private final ICompanyRepository companyRepository;
    private final ICompanyUserRepository companyUserRepository;
    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;
    private final IPermissionRepository permissionsRepository;

    public CompanyService(ICompanyRepository companyRepository, ICompanyUserRepository companyUserRepository, IRoleRepository roleRepository, IUserRepository userRepository, IPermissionRepository permissionsRepository) {
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.permissionsRepository = permissionsRepository;
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
        company.setType(companyDto.getType());
        company.setCompanyEmail(companyDto.getEmail());
        company.setCompanyPhone(companyDto.getPhone());
        company.setCompanyWebsite(companyDto.getWebsite());
        company.setIsActive(companyDto.isActive());
        company.setTenantId(uuid.toString());
        company.setCreatedAt(LocalDateTime.now());

        company = companyRepository.save(company);

        // 2. Convertir los Sets a Lists para mantener el orden
        List<KeyValueDto> roles = new ArrayList<>(companyDto.getRoles());
        List<Long> userIds = new ArrayList<>(companyDto.getUserIds());
        List<Long> permissions = new ArrayList<>(companyDto.getPermissions());

        // 3. Crear las relaciones
        for (int i = 0; i < roles.size(); i++) {
            KeyValueDto roleDto = roles.get(i);
            Long userId = userIds.get(i);
            Long permissionId = permissions.get(i);

            // Obtener rol
            RoleEntity role = roleRepository.findById(roleDto.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleDto.getId()));

            // Obtener usuario
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            // Obtener permiso
            PermissionsEntity permission = permissionsRepository.findById(permissionId)
                    .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

            // Crear la relación
            CompanyUserRoleEntity companyUserRole = new CompanyUserRoleEntity();
            companyUserRole.setCompany(company);
            companyUserRole.setUser(user);
            companyUserRole.setRole(role);
            companyUserRole.setPermissions(permission);
            companyUserRole.setStatus("ACTIVE");
            companyUserRole.setCreatedAt(LocalDateTime.now());

            companyUserRepository.save(companyUserRole);
        }

        log.info("Company with tenantId {} created", uuid);
        CompanyDto savedCompanyDto = new CompanyDto();
        responseCompanyDto(savedCompanyDto, company);
        return savedCompanyDto;
    }

    @Transactional
    public void updateCompany(Long id, CompanyDto companyDto) {
        log.info("Updating company with id: {}", id);

        CompanyEntity existingEntity = companyRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No Company were found with the id %s", id)));

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

        companyRepository.save(existingEntity);

        // Convertir los Sets a Lists para mantener el orden
        List<KeyValueDto> roles = new ArrayList<>(companyDto.getRoles());
        List<Long> userIds = new ArrayList<>(companyDto.getUserIds());
        List<Long> permissions = new ArrayList<>(companyDto.getPermissions());

        // Obtener todas las relaciones existentes para esta compañía
        List<CompanyUserRoleEntity> existingRelations = companyUserRepository.findByCompanyId(id);

        // Eliminar todas las relaciones existentes que no están en el nuevo request
        for (CompanyUserRoleEntity relation : existingRelations) {
            if (!userIds.contains(relation.getUser().getId())) {
                companyUserRepository.delete(relation);
            }
        }

        // Actualizar o crear nuevas relaciones
        for (int i = 0; i < roles.size(); i++) {
            KeyValueDto roleDto = roles.get(i);
            Long userId = userIds.get(i);
            Long permissionId = permissions.get(i);

            // Obtener rol
            RoleEntity role = roleRepository.findById(roleDto.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found with id: " + roleDto.getId()));

            // Obtener usuario
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found with id: " + userId));

            // Obtener permiso
            PermissionsEntity permission = permissionsRepository.findById(permissionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permission not found with id: " + permissionId));

            // Buscar si ya existe la relación
            CompanyUserRoleEntity existingRelation = existingRelations.stream()
                    .filter(relation -> relation.getUser().getId().equals(userId))
                    .findFirst()
                    .orElse(null);

            if (existingRelation != null) {
                // Actualizar relación existente
                existingRelation.setRole(role);
                existingRelation.setPermissions(permission);
                companyUserRepository.save(existingRelation);
            } else {
                // Crear nueva relación
                CompanyUserRoleEntity newRelation = new CompanyUserRoleEntity();
                newRelation.setCompany(existingEntity);
                newRelation.setUser(user);
                newRelation.setRole(role);
                newRelation.setPermissions(permission);
                newRelation.setStatus("ACTIVE");
                newRelation.setCreatedAt(LocalDateTime.now());
                companyUserRepository.save(newRelation);
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
        dto.setType(Objects.equals(CompanyTypeEnum.EMPRESA.toString(), entity.getType()) ? CompanyTypeEnum.EMPRESA : CompanyTypeEnum.NATURAL);
        dto.setTenantId(entity.getTenantId());
        dto.setCreatedAt(entity.getCreatedAt().toLocalDate());
        dto.setWebsite(entity.getCompanyWebsite());
        dto.setIsActive(entity.getIsActive());

        // Obtener roles, usuarios y permisos
        List<KeyValueDto> roles = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();
        List<Long> permissions = new ArrayList<>();

        for (CompanyUserRoleEntity companyUserRole : entity.getCompanyUserEntity()) {
            roles.add(new KeyValueDto(companyUserRole.getRole().getId(), companyUserRole.getRole().getRoleName(), companyUserRole.getRole().getRoleDescription()));
            userIds.add(companyUserRole.getUser().getId());
            permissions.add(companyUserRole.getPermissions().getId());
        }

        dto.setRoles(new HashSet<>(roles));
        dto.setUserIds(userIds);
        dto.setPermissions(permissions);
    }

}
