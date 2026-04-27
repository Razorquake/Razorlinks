package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.UrlRedirectCache;
import com.razorquake.razorlinks.models.UrlMapping;
import com.razorquake.razorlinks.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UrlRedirectLookupService {
    private final UrlMappingRepository urlMappingRepository;

    @Cacheable(cacheNames = "redirects", key = "#shortLink", unless = "#result == null")
    public UrlRedirectCache resolve(String shortLink) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortLink);

        if (urlMapping == null) {
            return null;
        }

        return new UrlRedirectCache(
                urlMapping.getId(),
                urlMapping.getShortUrl(),
                urlMapping.getOriginalUrl(),
                urlMapping.getUser().getUsername()
        );
    }

}
