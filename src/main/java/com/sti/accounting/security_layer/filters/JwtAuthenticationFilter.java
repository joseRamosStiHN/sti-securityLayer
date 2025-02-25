package com.sti.accounting.security_layer.filters;


import com.sti.accounting.security_layer.core.CustomUserDetails;


import com.sti.accounting.security_layer.dto.UserDto;
import com.sti.accounting.security_layer.service.JwtServiceImplement;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtServiceImplement jwtService;

    public JwtAuthenticationFilter(JwtServiceImplement jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String token = extractTokenFromCookie(request);

        // if token is null or empty return with unauthorized
        if (token == null || token.isEmpty()) {
            logger.info("token not exist");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Error el token no proved\"}");
            response.getWriter().flush();
            response.getWriter().close();
            return;
        }

//         validate token
        if (!jwtService.isTokenValid(token)) {
            logger.info("token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Error el token a expirado\"}");
            response.getWriter().flush();
            response.getWriter().close();
            return;
        }


        try{
            UserDto userDetails = jwtService.getUserDetails(token);

            // Global roles of user
            List<SimpleGrantedAuthority> authorities = userDetails.getGlobalRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .toList();

            // create custom user
            CustomUserDetails customUserDetails = new CustomUserDetails(userDetails, authorities);
            //create SecurityContextHolder

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    customUserDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (RuntimeException e) {
            logger.error("Error al autenticar usuario desde token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Error al autenticar usuario desde token\"}");
            response.getWriter().flush();
            response.getWriter().close();
            return;
        }

        filterChain.doFilter(request, response);

    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("x-auth".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}