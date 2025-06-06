package com.sti.accounting.security_layer.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sti.accounting.security_layer.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImplement {

    private static final Logger logger = LoggerFactory.getLogger(JwtServiceImplement.class);

    @Value("${app.token.secret.key}")
    private String secretKey;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token) {
        boolean tokenExpired = isTokenExpired(token);
        logger.debug("tokenExpired: {}", tokenExpired);
        return !tokenExpired;
    }


    public UserDto getUserDetails(String token) {
        Claims claims = extractAllClaims(token);
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Ignora propiedades desconocidas

        Map<String, Object> userMap = claims.get("user", Map.class);
        return objectMapper.convertValue(userMap, UserDto.class);
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        Date date = extractClaim(token, Claims::getExpiration);
        logger.debug("extractExpiration: {}", date);
        logger.debug("extractExpiration: {}", date.getTime());
        return date;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}