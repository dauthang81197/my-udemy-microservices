package com.thanghub.courseservice.userCourse.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserService {

    public UUID getUserIdClaim(Jwt jwt) throws IllegalAccessException {
        String userIdClaim = jwt.getClaim("userId");

        if (userIdClaim == null) {
            throw new IllegalAccessException("Token does not contain userId claim. Please re-login.");
        }

        return UUID.fromString(userIdClaim);
    }
}
