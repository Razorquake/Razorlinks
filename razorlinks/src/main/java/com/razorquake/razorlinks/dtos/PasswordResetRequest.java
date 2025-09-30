package com.razorquake.razorlinks.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetRequest {
    private String token;
    @NotBlank
    @Size(min = 6, max = 40)
    private String newPassword;
}
