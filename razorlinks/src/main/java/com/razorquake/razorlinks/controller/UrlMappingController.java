package com.razorquake.razorlinks.controller;

import com.google.zxing.WriterException;
import com.razorquake.razorlinks.dtos.ClickAnalyticsFilter;
import com.razorquake.razorlinks.dtos.ClickEventDTO;
import com.razorquake.razorlinks.dtos.UrlMappingDTO;
import com.razorquake.razorlinks.dtos.UrlMappingFilter;
import com.razorquake.razorlinks.models.User;
import com.razorquake.razorlinks.service.QRCodeService;
import com.razorquake.razorlinks.service.UrlMappingService;
import com.razorquake.razorlinks.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class UrlMappingController {

    private final UrlMappingService urlMappingService;
    private final UserService userService;
    private final QRCodeService qrCodeService;

    @PostMapping("/shorten")
    public ResponseEntity<UrlMappingDTO> createShortUrl(
            @RequestBody Map<String, String> request, Principal principal
    ){
        String originalUrl = request.get("originalUrl");
        User user = userService.findByUsername(principal.getName());
        UrlMappingDTO urlMappingDTO = urlMappingService.createShortUrl(originalUrl, user);
        return ResponseEntity.ok(urlMappingDTO);
    }

    @GetMapping("/myurls")
    public ResponseEntity<Page<UrlMappingDTO>> getMyUrls(
            Principal principal,
            @ModelAttribute @ParameterObject UrlMappingFilter filter
    ){
        User user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(urlMappingService.getUrlsByUser(user, filter));
    }

    @GetMapping("/analytics/{shortUrl}")
    public ResponseEntity<Page<ClickEventDTO>> getUrlAnalytics(
            @PathVariable String shortUrl,
            @ModelAttribute @ParameterObject ClickAnalyticsFilter filter
    ){
        return ResponseEntity.ok(urlMappingService.getClickEventByDate(shortUrl, filter));
    }

    @GetMapping("/totalClicks")
    public ResponseEntity<Page<ClickEventDTO>> getTotalClicksByDate(
            Principal principal,
            @ModelAttribute @ParameterObject ClickAnalyticsFilter filter
    ) {
        User user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(urlMappingService.getTotalClicksByUserAndDate(user, filter));
    }

    @DeleteMapping("/{shortUrl}")
    public ResponseEntity<?> deleteUrlMapping(
            @PathVariable String shortUrl, Principal principal
    ){
        User user = userService.findByUsername(principal.getName());
        urlMappingService.deleteUrlMapping(shortUrl, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/qr/{shortUrl}")
    public ResponseEntity<byte[]> getQRCode(
            @PathVariable String shortUrl,
            @RequestParam(defaultValue = "300") int size
    ) {
        try {
            byte[] qrCode = qrCodeService.generateQRCode(shortUrl, size, size);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCode.length);

            return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);
        } catch (WriterException | IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
