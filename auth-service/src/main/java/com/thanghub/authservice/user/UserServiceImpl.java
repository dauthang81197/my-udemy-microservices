package com.thanghub.authservice.user;

import com.thanghub.authservice.common.enums.UserStatusEnum;
import com.thanghub.authservice.security.JwtService;
import com.thanghub.authservice.user.request.LoginRequestDto;
import com.thanghub.authservice.user.request.RegisterRequestDto;
import com.thanghub.authservice.user.response.AuthResponseDto;
import com.thanghub.authservice.user.response.RegisterResponseDto;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public AuthResponseDto login(LoginRequestDto loginRequest) {

        // Step 1: Spring Security automation check email + password
        authenticationManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        // Step 2: Check user from Database
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginRequest.getEmail()));
        user.setPassword(null); // Set password null before return to client
        // Step 3: Generate JWT token
        String accessToken = jwtService.generateToken(user);
        return new AuthResponseDto(accessToken, null, user);

    }

    @Override
    public RegisterResponseDto register(RegisterRequestDto registerRequestDto) throws BadRequestException {

        // Step 1: Check email exist in database
        Optional<User> user = userRepository.findByEmail(registerRequestDto.getEmail());
        if (user.isPresent()) {
            throw new BadRequestException("Email already exists: " + registerRequestDto.getEmail());
        }

        // Step 2: Hash password
        User newUser = User.builder()
                .email(registerRequestDto.getEmail())
                .username(registerRequestDto.getUsername())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .full_name(registerRequestDto.getFullName())
                .status(UserStatusEnum.ACTIVE)
                .build();

        // Step 3: Save Database
        User savedUser = userRepository.save(newUser);

        // Step 4: Return response
        return new RegisterResponseDto(savedUser.getUsername(), savedUser.getEmail());
    }

    @Override
    public User createUser(User user) {
        return null;
    }

    @Override
    public User getUserById(Long id) {
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        return null;
    }
}
