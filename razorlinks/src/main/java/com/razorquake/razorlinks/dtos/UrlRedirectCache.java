package com.razorquake.razorlinks.dtos;

public record UrlRedirectCache(
        Long id,
        String shortUrl,
        String originalUrl,
        String username
) {
}
