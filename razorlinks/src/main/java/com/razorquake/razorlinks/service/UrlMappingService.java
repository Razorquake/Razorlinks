package com.razorquake.razorlinks.service;

import com.razorquake.razorlinks.dtos.ClickEventDTO;
import com.razorquake.razorlinks.dtos.UrlMappingDTO;
import com.razorquake.razorlinks.models.ClickEvent;
import com.razorquake.razorlinks.models.UrlMapping;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.repository.ClickEventRepository;
import com.razorquake.razorlinks.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlMappingService {

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

    public List<UrlMappingDTO> getUrlsByUser(User user) {
        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<ClickEventDTO> getClickEventByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping != null) {
            return clickEventRepository.findByUrlMappingAndClickDateBetween(urlMapping, start, end).stream()
                    .collect(Collectors.groupingBy(clickEvent -> clickEvent.getClickDate().toLocalDate(), Collectors.counting()))
                    .entrySet().stream().map(
                            entry -> {
                                ClickEventDTO clickEventDTO = new ClickEventDTO();
                                clickEventDTO.setClickDate(entry.getKey());
                                clickEventDTO.setCount(entry.getValue());
                                return clickEventDTO;
                            }
                    ).toList();
        }
        return null;
    }

    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);
        return clickEventRepository
                .findByUrlMappingInAndClickDateBetween(
                        urlMappings,
                        start.atStartOfDay(),
                        end.plusDays(1).atStartOfDay()).stream()
                .collect(Collectors.groupingBy(clickEvent -> clickEvent.getClickDate().toLocalDate(), Collectors.counting()));
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
}
