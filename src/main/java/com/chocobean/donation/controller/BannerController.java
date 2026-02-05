package com.chocobean.donation.controller;

import com.chocobean.donation.dto.InsertBanner;
import com.chocobean.donation.dto.InsertDonation;
import com.chocobean.donation.dto.ReportHistory;
import com.chocobean.donation.service.BannerService;
import com.chocobean.donation.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BannerController {

    private final BannerService bannerService;
    private final UserService userService;

    @PostMapping("/admin/insertBanner")
    public ResponseEntity<?> insertBanner(
            @RequestPart("banner") InsertBanner insertBanner,
            @RequestPart(value = "bannerImg", required = false) MultipartFile bannerImg,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }

        String userId = userDetails.getUsername();
        int role = userService.getRoleByUserName(userId);
        if (role != 0) {
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }

        String imageUrl = null;
        if (bannerImg != null && !bannerImg.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + bannerImg.getOriginalFilename();
                File saveFile = new File("C:/Users/kmcsl/OneDrive/Desktop/KH/연습/img/" + fileName);
                bannerImg.transferTo(saveFile);
                imageUrl = "/images/" + fileName;
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
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }

        String userId = userDetails.getUsername();
        int role = userService.getRoleByUserName(userId);


        if (role == 0) {
            List<InsertBanner> banners = bannerService.findAll();
            return ResponseEntity.ok(banners);
        }else {
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }
    }

    @GetMapping("/public/mainBanner")
    public ResponseEntity<?> mainBanner() {
        List<InsertBanner> activeBanners = bannerService.findActiveBanners();

        return ResponseEntity.ok(activeBanners);

    }

}
