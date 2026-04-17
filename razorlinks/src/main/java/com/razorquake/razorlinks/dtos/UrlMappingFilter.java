package com.razorquake.razorlinks.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Query parameters for filtering and paging URL mappings")
public class UrlMappingFilter extends PageFilter {
    @Schema(description = "Matches short URL or original URL", example = "example")
    private String search;

    @Schema(description = "Short URL contains filter", example = "abc123")
    private String shortUrl;

    @Schema(description = "Original URL contains filter", example = "example.com")
    private String originalUrl;

    @Schema(description = "Minimum click count", example = "10")
    private Integer minClickCount;

    @Schema(description = "Maximum click count", example = "100")
    private Integer maxClickCount;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Inclusive start date for URL creation time", example = "2026-04-01")
    private LocalDate createdStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Inclusive end date for URL creation time", example = "2026-04-30")
    private LocalDate createdEndDate;
}
