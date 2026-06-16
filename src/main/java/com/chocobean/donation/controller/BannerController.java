package com.chocobean.donation.controller;

import com.chocobean.donation.dto.InsertBanner;
import com.chocobean.donation.dto.ReportHistory;
import com.chocobean.donation.service.BannerService;
import com.chocobean.donation.service.FileUploadService;
import com.chocobean.donation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BannerController {

    private final BannerService bannerService;
    private final UserService userService;
    private final FileUploadService fileUploadService;

    @PostMapping("/admin/insertBanner")
    public ResponseEntity<?> insertBanner(
            @RequestPart("banner") InsertBanner insertBanner,
            @RequestPart(value = "bannerImg", required = false) MultipartFile bannerImg,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        String imageUrl = null;
        if (bannerImg != null && !bannerImg.isEmpty()) {
            try {
                imageUrl = fileUploadService.uploadImage(bannerImg);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("FILE_UPLOAD_FAILED");
            }
        }

        insertBanner.setBannerImg(imageUrl);
        bannerService.insertBanner(insertBanner);

        return ResponseEntity.ok("ok");
    }

    @GetMapping("/admin/bannerState")
    public ResponseEntity<?> bannerState(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<InsertBanner> banners = bannerService.findAll();
        return ResponseEntity.ok(banners);
    }

    @GetMapping("/public/mainBanner")
    public ResponseEntity<?> mainBanner() {
        List<InsertBanner> activeBanners = bannerService.findActiveBanners();

        return ResponseEntity.ok(activeBanners);

    }

}
