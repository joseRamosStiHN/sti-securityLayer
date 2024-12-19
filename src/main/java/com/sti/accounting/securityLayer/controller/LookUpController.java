package com.sti.accounting.securityLayer.controller;

import com.sti.accounting.securityLayer.dto.KeyValueDto;
import com.sti.accounting.securityLayer.service.LookUpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    @GetMapping("/permissions")
    public List<KeyValueDto> getPermissions() {
        log.info("getPermissions");
        return lookUpService.getPermission();
    }
}
