package com.sti.accounting.security_layer;

import com.sti.accounting.security_layer.dto.CreateUserDto;
import com.sti.accounting.security_layer.dto.KeyValueDto;
import com.sti.accounting.security_layer.entities.PermissionsEntity;
import com.sti.accounting.security_layer.entities.RoleEntity;
import com.sti.accounting.security_layer.repository.IPermissionRepository;
import com.sti.accounting.security_layer.repository.IRoleRepository;
import com.sti.accounting.security_layer.repository.IUserRepository;
import com.sti.accounting.security_layer.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SecurityLayerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityLayerApplication.class, args);
    }

    @Bean
    @Transactional
    CommandLineRunner seed(IUserRepository userRepository,
                           IRoleRepository roleRepository,
                           UserService userService,
                           IPermissionRepository permissionRepository) {
        return args -> {
            try {
                seedRoles(roleRepository);
                seedUsers(userService, userRepository);
                seedPermissions(permissionRepository);
            } catch (Exception e) {
                // Log the error but don't prevent application startup
                System.err.println("Error during seeding: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    private void seedRoles(IRoleRepository roleRepository) {
        if (roleRepository.count() == 0) {
            List<RoleEntity> roles = Arrays.asList(
                    new RoleEntity(1L, "ADMINISTRADOR", true, "Rol administrador global del sistema"),
                    new RoleEntity(2L, "CONSULTA", true, "Rol de consulta global del sistema"),
                    new RoleEntity(3L, "REGISTRO CONTABLE", false, "Rol de registro contable del modulo"),
                    new RoleEntity(4L, "APROBADOR", false, "Rol de aprobacion contable del modulo"),
                    new RoleEntity(5L, "CONSULTA", false, "Rol de consulta contable del modulo")
            );
            roleRepository.saveAll(roles);
        }
    }

    private void seedUsers(UserService userService, IUserRepository userRepository) {
        if (userRepository.count() == 0) {
            CreateUserDto createUserDto = new CreateUserDto();
            createUserDto.setUserName("Admin");
            createUserDto.setFirstName("Administrador");
            createUserDto.setLastName("Sistema");
            createUserDto.setEmail("admin@email.com");
            createUserDto.setUserAddress("direccion");
            createUserDto.setUserPhone("+504 99964587");
            createUserDto.setPassword("admin");
            createUserDto.setIsActive(true);
            KeyValueDto roleUser = new KeyValueDto();
            roleUser.setId(1L);
            createUserDto.setGlobalRoles(List.of(roleUser));
            userService.createUser(createUserDto);
        }
    }

    private void seedPermissions(IPermissionRepository permissionRepository) {
        if (permissionRepository.count() == 0) {
            List<PermissionsEntity> permissions = Arrays.asList(
                    new PermissionsEntity(1L, "DATA_ENTRY", "ENTRADA DE DATOS"),
                    new PermissionsEntity(2L, "APPROVE", "APROBAR"),
                    new PermissionsEntity(3L, "FULL_ACCESS", "ACCESO COMPLETO")
            );
            permissionRepository.saveAll(permissions);
        }
    }
}
