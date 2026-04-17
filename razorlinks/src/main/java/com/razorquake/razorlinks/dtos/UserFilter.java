package com.razorquake.razorlinks.dtos;

import com.razorquake.razorlinks.models.AppRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Query parameters for filtering and paging user results")
public class UserFilter extends PageFilter {
    @Schema(description = "Matches username or email", example = "demo")
    private String search;

    @Schema(description = "Username contains filter", example = "demo")
    private String username;

    @Schema(description = "Email contains filter", example = "example.com")
    private String email;

    @Schema(description = "Role filter", example = "ROLE_ANALYST")
    private AppRole roleName;

    @Schema(description = "Whether the account is enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Whether the account is not locked", example = "true")
    private Boolean accountNonLocked;

    @Schema(description = "Whether the account is not expired", example = "true")
    private Boolean accountNonExpired;

    @Schema(description = "Whether the credentials are not expired", example = "true")
    private Boolean credentialsNonExpired;

    @Schema(description = "Whether two-factor authentication is enabled", example = "false")
    private Boolean twoFactorEnabled;

    @Schema(description = "Signup method filter", example = "email")
    private String signUpMethod;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Inclusive start date for user creation time", example = "2026-04-01")
    private LocalDate createdStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Inclusive end date for user creation time", example = "2026-04-30")
    private LocalDate createdEndDate;
}
