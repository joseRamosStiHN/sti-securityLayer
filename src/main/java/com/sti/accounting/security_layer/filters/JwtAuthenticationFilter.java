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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String[] PUBLIC_ROUTES = {
            "/api/v1/login",
            "/api/v1/login/**",
            "/api/v1/user/recover-password"
    };

    private final JwtServiceImplement jwtService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtServiceImplement jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return Arrays.stream(PUBLIC_ROUTES)
                .anyMatch(p -> pathMatcher.match(p, path));
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
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Error: token no encontrado");
            return;
        }

        // validate token
        if (!jwtService.isTokenValid(token)) {
            logger.info("token expired");
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Error: el token ha expirado");
            return;
        }

        try {
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
            filterChain.doFilter(request, response);

        } catch (RuntimeException e) {
            logger.error("Error al autenticar usuario desde token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error al autenticar usuario desde token");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
        response.getWriter().flush();
        response.getWriter().close();
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
