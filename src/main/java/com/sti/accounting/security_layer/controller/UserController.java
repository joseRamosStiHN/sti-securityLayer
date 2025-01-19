package com.sti.accounting.security_layer.controller;

import com.sti.accounting.security_layer.dto.CreateUserDto;
import com.sti.accounting.security_layer.dto.UserDto;
import com.sti.accounting.security_layer.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @PutMapping("/{id}")
    public void updateUser(@PathVariable Long id, @RequestBody CreateUserDto updateUserDto) {
        log.info("Update user with id {}", id);
        //userService.updateUser(id, updateUserDto);
    }


}
