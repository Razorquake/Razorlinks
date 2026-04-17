package com.razorquake.razorlinks.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Query parameters for paging and sorting click analytics")
public class ClickAnalyticsFilter extends PageFilter {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Inclusive start timestamp", example = "2026-04-01T00:00:00")
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Schema(description = "Inclusive end timestamp", example = "2026-04-30T23:59:59")
    private LocalDateTime endDate;

    public ClickAnalyticsFilter() {
        setSortBy("clickDate");
        setSortOrder("ASC");
    }
}
