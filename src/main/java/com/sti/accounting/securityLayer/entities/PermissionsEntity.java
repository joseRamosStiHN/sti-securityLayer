package com.sti.accounting.securityLayer.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PERMISSIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // DATA_ENTRY, APPROVE, FULL_ACCESS
    private String description;

    public PermissionsEntity(Long id) {
        this.id = id;
    }
}
