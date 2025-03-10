package com.sti.accounting.security_layer.controller;

import com.sti.accounting.security_layer.core.JwtService;
import com.sti.accounting.security_layer.dto.LoginDto;
import com.sti.accounting.security_layer.dto.UserDto;
import com.sti.accounting.security_layer.service.LoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/login")
@CrossOrigin("*")
public class LoginController {

    @Value("${app.https}")
    private boolean isHttps;

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
        httpServletResponse.addCookie(cookie);

        return userDto;
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse httpServletResponse) {

        Cookie cookie = new Cookie("x-auth", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setSecure(isHttps);
        httpServletResponse.addCookie(cookie);

        return ResponseEntity.ok("Logout exitoso");
    }
}
