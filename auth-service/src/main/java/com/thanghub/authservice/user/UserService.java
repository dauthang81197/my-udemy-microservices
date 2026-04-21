package com.thanghub.authservice.user;

import com.thanghub.authservice.user.request.LoginRequestDto;
import com.thanghub.authservice.user.request.RegisterRequestDto;
import com.thanghub.authservice.user.response.AuthResponseDto;
import com.thanghub.authservice.user.response.RegisterResponseDto;
import org.apache.coyote.BadRequestException;

public interface UserService {
    AuthResponseDto login(LoginRequestDto loginRequest);

    RegisterResponseDto register(RegisterRequestDto registerRequestDto) throws BadRequestException;

    User createUser(User user);

    User getUserById(Long id);

    User getUserByEmail(String email);

}