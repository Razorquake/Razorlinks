package com.razorquake.razorlinks.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PageFilter {
    @Schema(description = "Field name used for sorting", example = "createdDate", defaultValue = "createdDate")
    private String sortBy = "createdDate";

    @Schema(description = "Sort direction", example = "DESC", defaultValue = "DESC")
    private String sortOrder = "DESC";

    @Schema(description = "Zero-based page index", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Number of records per page", example = "10", defaultValue = "10")
    private int size = 10;
}
