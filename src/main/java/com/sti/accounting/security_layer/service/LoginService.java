package com.sti.accounting.security_layer.service;

import com.sti.accounting.security_layer.dto.LoginDto;
import com.sti.accounting.security_layer.dto.UserDto;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final UserService userService;

    public LoginService(UserService userService) {
        this.userService = userService;
    }

    public UserDto loginUser(LoginDto login) {
        return userService.getUserByUserNameAndPassword(login.getUserName(), login.getPassword());
    }

}
