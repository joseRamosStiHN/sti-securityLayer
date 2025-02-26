package com.sti.accounting.security_layer.controller;


import com.sti.accounting.security_layer.dto.CompanyByUser;
import com.sti.accounting.security_layer.dto.CompanyDto;
import com.sti.accounting.security_layer.dto.pageble.PageResponse;
import com.sti.accounting.security_layer.dto.pageble.PageResponseDto;
import com.sti.accounting.security_layer.service.AuthService;
import com.sti.accounting.security_layer.service.CompanyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company")
public class CompanyController {

    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);
    private final CompanyService companyService;
    private final AuthService authService;

    public CompanyController(CompanyService companyService, AuthService authService) {
        this.companyService = companyService;
        this.authService = authService;
    }

    @GetMapping("/")
    public List<CompanyDto> getAllCompany() {
        log.info("Getting all companies");
        return companyService.getAllCompany();
    }

    @GetMapping("/company-user")
    public ResponseEntity<? extends PageResponse<CompanyByUser>> getAllCompanyByUser(
            @RequestParam(required = false, defaultValue = "0") Integer page ,
            @RequestParam(required = false, defaultValue = "9") Integer size) {
        
        Long userId = this.authService.getUserId();
        Page<CompanyByUser> company = companyService.getAllCompanyByUser(page, size, userId);
        
        PageResponseDto<CompanyByUser> pageResponseDto = new PageResponseDto<>();

        return pageResponseDto.buildResponseEntity(company.getSize(), company.getNumberOfElements(),
                company.getTotalPages(), company.getNumber(), company.getContent());

    }

    @GetMapping("/user/{id}")
    public CompanyByUser getCompanyByUser(@PathVariable Long id ) {
        Long userId = this.authService.getUserId();
       return companyService.getCompanyByUser(userId,id);


    }

    @GetMapping("/logo/{id}")
    public ResponseEntity<byte[]> getCompanyLogo(@PathVariable Long id) {
        byte[] logo = companyService.getCompanyLogoById(id);
        if (logo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(logo);
    }


    @GetMapping("/{id}")
    public CompanyDto getCompanyById(@PathVariable Long id) {
        log.info("Getting company by id: {}", id);
        return companyService.getCompanyById(id);
    }

    @PostMapping()
    public CompanyDto saveCompany(@RequestBody CompanyDto companyDto) {
        log.info("Saving company: {}", companyDto);
        return companyService.saveCompany(companyDto);
    }

    @PutMapping("/{id}/{actionByUser}")
    public void updateCompany(@PathVariable Long id, @PathVariable Long actionByUser, @RequestBody CompanyDto companyDto) {
        log.info("Updating company with id: {}", id);
        companyService.updateCompany(id, actionByUser, companyDto);
    }


}
