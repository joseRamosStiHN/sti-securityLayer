package com.sti.accounting.security_layer.controller;

import com.sti.accounting.security_layer.dto.CompanyByUser;
import com.sti.accounting.security_layer.dto.CreateUserDto;
import com.sti.accounting.security_layer.dto.UserDto;
import com.sti.accounting.security_layer.dto.pageble.PageResponse;
import com.sti.accounting.security_layer.dto.pageble.PageResponseDto;
import com.sti.accounting.security_layer.entities.UserEntity;
import com.sti.accounting.security_layer.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/")
    public List<UserDto> getAllUsers() {
        log.info("Get all users");
        return userService.getAllUsers();
    }


    @GetMapping("/by-company/{id}")
    public List<UserDto> getAllCompanyByUser(@PathVariable long id){

        return userService.getUsersByComapany(id);

    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.info("Get user by id {}", id);
        return userService.getUserById(id);
    }

    @PostMapping("/")
    public void createUser(@RequestBody CreateUserDto createUserDto) {
        log.info("Create user");
        userService.createUser(createUserDto);
    }

    @PutMapping("/{id}/{actionByUser}")
    public void updateUser(@PathVariable Long id, @PathVariable Long actionByUser, @RequestBody CreateUserDto updateUserDto) {
        log.info("Update user with id {}", id);
        userService.updateUser(id, actionByUser, updateUserDto);
    }


}
