package com.sti.accounting.securityLayer.service;

import com.sti.accounting.securityLayer.dto.LoginDto;
import com.sti.accounting.securityLayer.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

    private final UserService userService;

    public LoginService(UserService userService) {

        this.userService = userService;
    }

    public UserDto loginUser(LoginDto login) {
        return userService.getUserByUserNameAndPassword(login.getUserName(), login.getPassword());
    }

}
