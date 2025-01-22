package com.sti.accounting.security_layer.controller;

import com.sti.accounting.security_layer.dto.CompanyDto;
import com.sti.accounting.security_layer.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company")
public class CompanyController {

    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/")
    public List<CompanyDto> getAllCompany() {
        log.info("Getting all companies");
        return companyService.getAllCompany();
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
