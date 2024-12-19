package com.sti.accounting.securityLayer.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PERMISSIONS")
@Getter
@Setter
public class PermissionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // DATA_ENTRY, APPROVE, FULL_ACCESS
    private String description;

}
