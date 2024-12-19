package com.sti.accounting.securityLayer;

import com.sti.accounting.securityLayer.dto.CreateUserDto;
import com.sti.accounting.securityLayer.dto.KeyValueDto;
import com.sti.accounting.securityLayer.entities.RoleEntity;
import com.sti.accounting.securityLayer.repository.IRoleRepository;
import com.sti.accounting.securityLayer.repository.IUserRepository;

import com.sti.accounting.securityLayer.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class SecurityLayerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityLayerApplication.class, args);
	}
	//IRoleRepository roleRepository, IUserRoleRepository userRoleRepository
	@Bean
	CommandLineRunner seed(IUserRepository userRepository,IRoleRepository roleRepository, UserService userService){

		return args -> {
			RoleEntity role = new RoleEntity();
			if(roleRepository.count() == 0){
				role.setRoleName("ADMIN");
				role.setGlobal(true);
				roleRepository.save(role);
			}

			if(userRepository.count() == 0){
				CreateUserDto createUserDto = new CreateUserDto();
				createUserDto.setUserName("Demo");
				createUserDto.setFirstName("Demo");
				createUserDto.setLastName("Mode");
				createUserDto.setEmail("user@email.com");
				createUserDto.setPassword("demo");
				createUserDto.setActive(true);
				KeyValueDto roleUser = new KeyValueDto();
				roleUser.setId(1L);
				createUserDto.setRoles(List.of(roleUser));
				userService.createUser(createUserDto);

			}

		};
	}

}