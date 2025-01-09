package com.sti.accounting.securityLayer.service;

import com.sti.accounting.securityLayer.dto.KeyValueDto;
import com.sti.accounting.securityLayer.dto.PermissionDto;
import com.sti.accounting.securityLayer.dto.RoleDto;
import com.sti.accounting.securityLayer.entities.PermissionsEntity;
import com.sti.accounting.securityLayer.entities.RoleEntity;
import com.sti.accounting.securityLayer.repository.IPermissionRepository;
import com.sti.accounting.securityLayer.repository.IRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LookUpService {

    private static final Logger log = LoggerFactory.getLogger(LookUpService.class);
    private final IPermissionRepository permissionRepository;
    private final IRoleRepository roleRepository;

    public LookUpService(IPermissionRepository permissionRepository, IRoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }


    public List<KeyValueDto> getPermission() {
        log.info("getPermission");
        return permissionRepository.findAll().stream()
                .map(permission -> new KeyValueDto(permission.getId(), permission.getName(), permission.getDescription())).toList();
    }


    public PermissionsEntity createPermission(PermissionDto permissionDto) {
        log.info("Creating permission: {}", permissionDto);
        PermissionsEntity permission = new PermissionsEntity();
        permission.setName(permissionDto.getName());
        permission.setDescription(permissionDto.getDescription());
        return permissionRepository.save(permission);
    }

    public PermissionsEntity updatePermission(Long id, PermissionDto permissionDto) {
        log.info("Updating permission with id: {}", id);
        PermissionsEntity permission = permissionRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No Permission were found with the id %s", id)));

        permission.setName(permissionDto.getName());
        permission.setDescription(permissionDto.getDescription());
        return permissionRepository.save(permission);
    }


    public List<KeyValueDto> getRoles() {
        log.info("getRoles");
        return roleRepository.findAll().stream()
                .map(role -> new KeyValueDto(role.getId(), role.getRoleName(), role.getRoleDescription(), role.isGlobal())).toList();
    }

    public RoleEntity createRole(RoleDto roleDto) {
        log.info("Creating role: {}", roleDto);
        RoleEntity role = new RoleEntity();
        role.setRoleName(roleDto.getRoleName());
        role.setIsGlobal(roleDto.isGlobal());
        role.setRoleDescription(roleDto.getRoleDescription());
        return roleRepository.save(role);
    }

    public RoleEntity updateRole(Long id, RoleDto roleDto) {
        log.info("Updating role with id: {}", id);
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("No Role were found with the id %s", id)));

        role.setRoleName(roleDto.getRoleName());
        role.setIsGlobal(roleDto.isGlobal());
        role.setRoleDescription(roleDto.getRoleDescription());
        return roleRepository.save(role);
    }


}
