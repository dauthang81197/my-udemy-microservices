package com.thanghub.authservice.user;

import com.thanghub.authservice.common.ApiResponseBase;
import com.thanghub.authservice.user.UserService;
import com.thanghub.authservice.user.request.LoginRequestDto;
import com.thanghub.authservice.user.request.RegisterRequestDto;
import com.thanghub.authservice.user.response.AuthResponseDto;
import com.thanghub.authservice.user.response.RegisterResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Login",
            description = "Return JWT access token and refresh token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successfully"),
            @ApiResponse(responseCode = "401", description = "Login fail, please check email and password")
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<ApiResponseBase<AuthResponseDto>> login(@RequestBody LoginRequestDto request) {
        AuthResponseDto authResponseDto = userService.login(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Login successfully", authResponseDto));
    }


    @Operation(
            summary = "Register",
            description = "Return JWT access token and refresh token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successfully"),
            @ApiResponse(responseCode = "401", description = "Login fail, please check email and password")
    })
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<ApiResponseBase<RegisterResponseDto>> register(@RequestBody RegisterRequestDto request) throws BadRequestException {
        RegisterResponseDto registerResponseDto = userService.register(request);
        return ResponseEntity.ok(ApiResponseBase.ok("Login successfully", registerResponseDto));
    }
}
