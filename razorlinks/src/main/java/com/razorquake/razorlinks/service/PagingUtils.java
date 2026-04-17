package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.PageFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public final class PagingUtils {

    private PagingUtils() {
    }

    public static Pageable buildPageable(PageFilter filter, String defaultSortBy, Set<String> allowedSortFields) {
        String requestedSortBy = filter == null || filter.getSortBy() == null || filter.getSortBy().isBlank()
                ? defaultSortBy
                : filter.getSortBy();

        String sortBy = allowedSortFields.contains(requestedSortBy) ? requestedSortBy : defaultSortBy;
        Sort.Direction direction = filter != null && "ASC".equalsIgnoreCase(filter.getSortOrder())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        int page = filter == null ? 0 : Math.max(filter.getPage(), 0);
        int size = filter == null ? 10 : Math.clamp(filter.getSize(), 1, 100);

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    public static <T> Page<T> toPage(List<T> items, Pageable pageable) {
        int total = items.size();
        int start = Math.toIntExact(pageable.getOffset());

        if (start >= total) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        int end = Math.min(start + pageable.getPageSize(), total);
        return new PageImpl<>(items.subList(start, end), pageable, total);
    }
}
