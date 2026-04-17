package com.razorquake.razorlinks.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Query parameters for filtering and paging audit log results")
public class AuditLogFilter extends PageFilter {
    @Schema(description = "Matches action, username, or short URL", example = "created")
    private String search;

    @Schema(description = "Action filter", example = "SHORT_URL_CREATED")
    private String action;

    @Schema(description = "Username contains filter", example = "demo")
    private String username;

    @Schema(description = "Short URL contains filter", example = "abc123")
    private String shortUrl;

    @Schema(description = "Exact URL mapping id filter", example = "42")
    private Long urlMappingId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Inclusive start date for audit timestamps", example = "2026-04-01")
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Inclusive end date for audit timestamps", example = "2026-04-30")
    private LocalDate endDate;

    public AuditLogFilter() {
        setSortBy("timestamp");
    }
}
