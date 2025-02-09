package com.sti.accounting.security_layer.controller;

import com.sti.accounting.security_layer.core.JwtService;
import com.sti.accounting.security_layer.dto.LoginDto;
import com.sti.accounting.security_layer.dto.UserDto;
import com.sti.accounting.security_layer.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/login")
@CrossOrigin("*")
public class LoginController {

    @Value("${app.https}")
    private boolean isHttps;

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    private final LoginService loginService;
    private final JwtService jwtService;
    public LoginController(LoginService loginService, JwtService jwtService) {
        this.loginService = loginService;
        this.jwtService = jwtService;
    }


    @PostMapping()
    public UserDto login(HttpServletResponse httpServletResponse, @RequestBody @Validated LoginDto login){

        UserDto userDto = loginService.loginUser(login);

        String token = jwtService.generateToken(userDto);

        Cookie cookie = new Cookie("x-auth", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30);
        cookie.setSecure(isHttps);
       // cookie.setAttribute("SameSite", "Strict");
        httpServletResponse.addCookie(cookie);

        return userDto;
    }


}
