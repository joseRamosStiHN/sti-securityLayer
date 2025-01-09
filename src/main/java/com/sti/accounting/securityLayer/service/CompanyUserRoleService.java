package com.sti.accounting.securityLayer.service;

import com.sti.accounting.securityLayer.repository.ICompanyUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CompanyUserRoleService {

    private static final Logger log = LoggerFactory.getLogger(CompanyUserRoleService.class);
    private final ICompanyUserRepository companyUserRepository;


    public CompanyUserRoleService(ICompanyUserRepository companyUserRepository) {
        this.companyUserRepository = companyUserRepository;
    }


}
