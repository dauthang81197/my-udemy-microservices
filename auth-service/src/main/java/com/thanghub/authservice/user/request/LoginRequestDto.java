package com.thanghub.authservice.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    @Schema(
            description = "Email",
            example = "thangdau811@gmail.com"
    )
    private String email;
    @Schema(
            description = "Pass",
            example = "Admin@123"
    )
    private String password;
}
