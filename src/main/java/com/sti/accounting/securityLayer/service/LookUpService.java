package com.sti.accounting.securityLayer.service;

import com.sti.accounting.securityLayer.dto.KeyValueDto;
import com.sti.accounting.securityLayer.repository.IPermissionRepository;
import com.sti.accounting.securityLayer.repository.IRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
                .map(permission -> new KeyValueDto(permission.getId(), permission.getName(),permission.getDescription())).toList();
    }

    public List<KeyValueDto> getRoles() {
        log.info("getRoles");
        return roleRepository.findAll().stream()
                .map(role -> new KeyValueDto(role.getId(), role.getRoleName(), role.getRoleDescription())).toList();
    }


}
