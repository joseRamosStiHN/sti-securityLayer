package com.sti.accounting.security_layer.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sti.accounting.security_layer.dto.UserDto;

import java.util.List;
import java.util.Map;

public class JwtUtil {

    private static String secretKey;

    public static UserDto getUserDetails(String token) {

        DecodedJWT jwt = JWT.decode(token);
        Claim userClaim = jwt.getClaim("user");
        Map<String, Object> userMap = userClaim.asMap();
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper.convertValue(userMap, UserDto.class);
    }

}
