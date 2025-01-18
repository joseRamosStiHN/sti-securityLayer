package com.sti.accounting.security_layer.core;

import com.sti.accounting.security_layer.dto.UserDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
// tenanId
@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.token.time.expiration}")
    private Long jwtExpirationHours;

    @Value("${app.token.secret.key}")
    private String jwtSecret;

    public JwtService() {
    }

    public String generateToken(UserDto user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("user", user);
        return generateToken(claims, user);
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public boolean isTokenValid(String token, UserDto userInfo) {
        final String username = extractUsername(token);
        return (username.equals(userInfo.getUserName())) && !isTokenExpired(token);
    }


    public String generateToken(Map<String, Object> extraClaims, UserDto userInfo) {
        return buildToken(extraClaims, userInfo);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDto userInfo) {
        return Jwts.builder().claims(extraClaims)
                .subject(userInfo.getUserName())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * jwtExpirationHours))
                .signWith(getSigningKey())
                .compact();

    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
       return Jwts.parser()
               .verifyWith(getSigningKey())
               .build()
               .parseSignedClaims(token)
               .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
