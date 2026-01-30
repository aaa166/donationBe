package com.chocobean.donation.controller;

import com.chocobean.donation.dto.InsertBanner;
import com.chocobean.donation.dto.InsertDonation;
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

    @PostMapping("/admin/insertBanner")
    public ResponseEntity<?> insertBanner(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute InsertBanner insertBanner,
            @RequestParam("categories") String categoriesJson
            ) {
        // 📁 이미지 저장 폴더
        String uploadDir = "C:/Users/kmcsl/OneDrive/Desktop/KH/연습/img/";
        String imageUrl = null;

        // 1️⃣ 파일 처리
        if (image != null && !image.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

                File saveFile = new File(uploadDir + fileName);
                image.transferTo(saveFile);

                // DB에 저장할 경로 (프론트에서 접근할 URL 기준)
                imageUrl = "/images/" + fileName;

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.internalServerError().body("FILE_UPLOAD_FAILED");
            }
        }

        insertBanner.setBannerImg(imageUrl);

        // 2️⃣ 카테고리 JSON 파싱 + DB 저장
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> categoryNames =
                    mapper.readValue(categoriesJson, new TypeReference<List<String>>() {});

            bannerService.insertBanner(insertBanner, categoryNames);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("INVALID_DATA_OR_DB_ERROR");
        }

        return ResponseEntity.ok("ok");
    }

}
