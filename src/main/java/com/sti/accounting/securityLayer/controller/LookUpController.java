package com.sti.accounting.securityLayer.controller;

import com.sti.accounting.securityLayer.dto.KeyValueDto;
import com.sti.accounting.securityLayer.dto.PermissionDto;
import com.sti.accounting.securityLayer.dto.RoleDto;
import com.sti.accounting.securityLayer.entities.PermissionsEntity;
import com.sti.accounting.securityLayer.entities.RoleEntity;
import com.sti.accounting.securityLayer.service.LookUpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lookup")
public class LookUpController {


    private static final Logger log = LoggerFactory.getLogger(LookUpController.class);

    private final LookUpService lookUpService;

    public LookUpController(LookUpService lookUpService) {
        this.lookUpService = lookUpService;
    }

    @GetMapping("/roles")
    public List<KeyValueDto> getRoles() {
        log.info("getRoles");
        return lookUpService.getRoles();
    }

    @PostMapping("/roles")
    public ResponseEntity<RoleEntity> createRole(@RequestBody RoleDto roleDto) {
        log.info("Creating role: {}", roleDto);
        RoleEntity createdRole = lookUpService.createRole(roleDto);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<RoleEntity> updateRole(@PathVariable Long id, @RequestBody RoleDto roleDto) {
        log.info("Updating role with id: {}", id);
        RoleEntity updatedRole = lookUpService.updateRole(id, roleDto);
        return new ResponseEntity<>(updatedRole, HttpStatus.OK);
    }

    @GetMapping("/permissions")
    public List<KeyValueDto> getPermissions() {
        log.info("getPermissions");
        return lookUpService.getPermission();
    }

    @PostMapping("/permissions")
    public ResponseEntity<PermissionsEntity> createPermission(@RequestBody PermissionDto permissionDto) {
        log.info("Creating permission: {}", permissionDto);
        PermissionsEntity createdPermission = lookUpService.createPermission(permissionDto);
        return new ResponseEntity<>(createdPermission, HttpStatus.CREATED);
    }

    @PutMapping("/permissions/{id}")
    public ResponseEntity<PermissionsEntity> updatePermission(@PathVariable Long id, @RequestBody PermissionDto permissionDto) {
        log.info("Updating permission with id: {}", id);
        PermissionsEntity updatedPermission = lookUpService.updatePermission(id, permissionDto);
        return new ResponseEntity<>(updatedPermission, HttpStatus.OK);
    }
}
