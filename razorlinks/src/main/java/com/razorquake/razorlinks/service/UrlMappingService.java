package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.ClickAnalyticsFilter;
import com.razorquake.razorlinks.dtos.ClickEventDTO;
import com.razorquake.razorlinks.dtos.UrlMappingDTO;
import com.razorquake.razorlinks.dtos.UrlMappingFilter;
import com.razorquake.razorlinks.models.ClickEvent;
import com.razorquake.razorlinks.models.UrlMapping;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.ClickEventRepository;
import com.razorquake.razorlinks.repository.UrlMappingRepository;
import com.razorquake.razorlinks.repository.specification.UrlMappingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlMappingService {
    private static final Set<String> URL_MAPPING_SORT_FIELDS = Set.of(
            "createdDate",
            "shortUrl",
            "originalUrl",
            "clickCount"
    );
    private static final Set<String> ANALYTICS_SORT_FIELDS = Set.of("clickDate", "count");

    private final UrlMappingRepository urlMappingRepository;
    private final ClickEventRepository clickEventRepository;
    private final AuditLogService auditLogService;


    public void deleteUrlMapping(String shortUrl, User user) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping != null && urlMapping.getUser().getId().equals(user.getId())) {
            clickEventRepository.deleteAll(clickEventRepository.findByUrlMapping(urlMapping));
            urlMappingRepository.delete(urlMapping);
            auditLogService.shortURLDeleted(urlMapping);
        }
    }

    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateShortUrl();
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        UrlMapping savedUrlMapping = urlMappingRepository.save(urlMapping);
        auditLogService.shortURLCreated(savedUrlMapping);
        return convertToDto(savedUrlMapping);
    }

    private UrlMappingDTO convertToDto(UrlMapping urlMapping) {
        UrlMappingDTO urlMappingDTO = new UrlMappingDTO();
        urlMappingDTO.setId(urlMapping.getId());
        urlMappingDTO.setOriginalUrl(urlMapping.getOriginalUrl());
        urlMappingDTO.setShortUrl(urlMapping.getShortUrl());
        urlMappingDTO.setUsername(urlMapping.getUser().getUsername());
        urlMappingDTO.setCreateDate(urlMapping.getCreatedDate());
        urlMappingDTO.setClickCount(urlMapping.getClickCount());
        return urlMappingDTO;
    }

    private String generateShortUrl() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder shortUrl = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }
        return shortUrl.toString();
    }

    public Page<UrlMappingDTO> getUrlsByUser(User user, UrlMappingFilter filter) {
        Specification<UrlMapping> specification = UrlMappingSpecification.buildSpecification(user, filter);
        Pageable pageable = PagingUtils.buildPageable(filter, "createdDate", URL_MAPPING_SORT_FIELDS);
        return urlMappingRepository.findAll(specification, pageable).map(this::convertToDto);
    }

    public Page<ClickEventDTO> getClickEventByDate(String shortUrl, ClickAnalyticsFilter filter) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        Pageable pageable = PagingUtils.buildPageable(filter, "clickDate", ANALYTICS_SORT_FIELDS);

        if (urlMapping == null) {
            return PagingUtils.toPage(List.of(), pageable);
        }

        List<ClickEvent> clickEvents = resolveClickEventsByDate(urlMapping, filter.getStartDate(), filter.getEndDate());
        return buildAnalyticsPage(clickEvents, filter, pageable);
    }

    public Page<ClickEventDTO> getTotalClicksByUserAndDate(User user, ClickAnalyticsFilter filter) {
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);
        Pageable pageable = PagingUtils.buildPageable(filter, "clickDate", ANALYTICS_SORT_FIELDS);

        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingInAndClickDateBetween(
                urlMappings,
                resolveStartDate(filter.getStartDate()),
                resolveEndDateExclusive(filter.getEndDate())
        );

        return buildAnalyticsPage(clickEvents, filter, pageable);
    }

    public UrlMapping getOriginalUrl(String shortLink) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortLink);
        if (urlMapping != null){
            urlMapping.setClickCount(urlMapping.getClickCount()+1);
            urlMappingRepository.save(urlMapping);
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setClickDate(LocalDateTime.now());
            clickEvent.setUrlMapping(urlMapping);
            auditLogService.shortURLClicked(clickEventRepository.save(clickEvent));
        }
        return urlMapping;
    }

    private List<ClickEvent> resolveClickEventsByDate(UrlMapping urlMapping, LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null) {
            return clickEventRepository.findByUrlMappingAndClickDateBetween(urlMapping, start, end);
        }

        return clickEventRepository.findByUrlMapping(urlMapping).stream()
                .filter(clickEvent -> start == null || !clickEvent.getClickDate().isBefore(start))
                .filter(clickEvent -> end == null || !clickEvent.getClickDate().isAfter(end))
                .toList();
    }

    private Page<ClickEventDTO> buildAnalyticsPage(List<ClickEvent> clickEvents,
                                                   ClickAnalyticsFilter filter,
                                                   Pageable pageable) {
        Comparator<ClickEventDTO> comparator = "count".equals(filter.getSortBy())
                ? Comparator.comparing(ClickEventDTO::getCount).thenComparing(ClickEventDTO::getClickDate)
                : Comparator.comparing(ClickEventDTO::getClickDate).thenComparing(ClickEventDTO::getCount);

        if (!"ASC".equalsIgnoreCase(filter.getSortOrder())) {
            comparator = comparator.reversed();
        }

        List<ClickEventDTO> analytics = clickEvents.stream()
                .collect(Collectors.groupingBy(clickEvent -> clickEvent.getClickDate().toLocalDate(), Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    ClickEventDTO clickEventDTO = new ClickEventDTO();
                    clickEventDTO.setClickDate(entry.getKey());
                    clickEventDTO.setCount(entry.getValue());
                    return clickEventDTO;
                })
                .sorted(comparator)
                .toList();

        return PagingUtils.toPage(analytics, pageable);
    }

    private LocalDateTime resolveStartDate(LocalDateTime startDate) {
        return startDate == null ? LocalDate.of(1970, 1, 1).atStartOfDay() : startDate;
    }

    private LocalDateTime resolveEndDateExclusive(LocalDateTime endDate) {
        return endDate == null ? LocalDate.now().plusYears(100).atStartOfDay() : endDate.plusSeconds(1);
    }
}
