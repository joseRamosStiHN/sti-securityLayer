package com.sti.accounting.securityLayer.service;

import com.sti.accounting.securityLayer.dto.CompanyDto;

import com.sti.accounting.securityLayer.entities.CompanyEntity;
import com.sti.accounting.securityLayer.repository.ICompanyRepository;
import com.sti.accounting.securityLayer.utils.CompanyTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Service
public class CompanyService {


    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);
    private final ICompanyRepository companyRepository;

    public CompanyService(ICompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<CompanyDto> getAllCompany() {
        log.info("Getting all companies");
        return companyRepository.findAll().stream().map(x->{
            CompanyDto dto = new CompanyDto();
            dto.setId(x.getId());
            dto.setName(x.getCompanyName());
            dto.setAddress(x.getCompanyAddress());
            dto.setPhone(x.getCompanyPhone());
            dto.setEmail(x.getCompanyEmail());
            dto.setDescription(x.getCompanyDescription());
            dto.setRtn(x.getCompanyRTN());
            dto.setType(Objects.equals(CompanyTypeEnum.EMPRESA.toString(), x.getType()) ? CompanyTypeEnum.EMPRESA : CompanyTypeEnum.NATURAL);
            return dto;
        }).toList();
    }

    public CompanyDto getCompanyById(Long id) {
        log.info("Getting company by id: {}", id);
        CompanyEntity entity = companyRepository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        CompanyDto dto = new CompanyDto();
        dto.setId(entity.getId());
        dto.setName(entity.getCompanyName());
        dto.setAddress(entity.getCompanyAddress());
        dto.setPhone(entity.getCompanyPhone());
        dto.setEmail(entity.getCompanyEmail());
        dto.setDescription(entity.getCompanyDescription());
        dto.setRtn(entity.getCompanyRTN());
        dto.setType(Objects.equals(CompanyTypeEnum.EMPRESA.toString(), entity.getType()) ? CompanyTypeEnum.EMPRESA : CompanyTypeEnum.NATURAL);
        dto.setTenantId(entity.getTenantId().toString());
        return dto;
    }

    public void saveCompany(CompanyDto companyDto) {
        log.info("Saving company: {}", companyDto);
        UUID uuid = UUID.randomUUID();
        CompanyEntity entity = new CompanyEntity();
        entity.setCompanyName(companyDto.getName());
        entity.setCompanyAddress(companyDto.getAddress());
        entity.setCompanyPhone(companyDto.getPhone());
        entity.setCompanyEmail(companyDto.getEmail());
        entity.setCompanyDescription(companyDto.getDescription());
        entity.setCompanyRTN(companyDto.getRtn());
        entity.setType(companyDto.getType());
        entity.setTenantId(uuid.toString());
        companyRepository.save(entity);
        log.info("company with tenantId {} created", uuid);
    }
}
