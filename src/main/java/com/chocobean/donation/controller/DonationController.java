package com.chocobean.donation.controller;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.dto.DonationState;
import com.chocobean.donation.dto.DonationView;
import com.chocobean.donation.dto.InsertDonation;
import com.chocobean.donation.service.DonationService;
import com.chocobean.donation.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DonationController {

    private final DonationService donationService;
    private final UserService userService;

    @GetMapping("/public/donations")
    public List<DonationList> getDonations(@RequestParam(defaultValue = "0") Integer categoryId) {

        return donationService.getDonations(categoryId);
    }
    @GetMapping("/public/donationsDate")
    public List<DonationList> getDonationsDate() {
        return donationService.getDonationsOrderByDonationDeadlineDateAsc();
    }

    @GetMapping("/public/donationView/{donationNo}")
    public ResponseEntity<DonationView> getDonationViewByNo(
            @PathVariable("donationNo") Long no
    ) {
        DonationView donationData = donationService.getDonationByNo(no);

        return ResponseEntity.ok(donationData);
    }

    @GetMapping("/public/donationApply")
    public ResponseEntity<?> getDonationApply(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }

        String userName = userDetails.getUsername();
        int role = userService.getRoleByUserName(userName);
        if (role == 1){
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }else{
            return ResponseEntity.ok("ok");
        }
    }

    @PostMapping("/public/insertDonation")
    public ResponseEntity<?> insertDonation(
            @ModelAttribute InsertDonation insertDonation,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails,
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

        insertDonation.setDonationImg(imageUrl);

        // 2️⃣ 카테고리 JSON 파싱 + DB 저장
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> categoryNames =
                    mapper.readValue(categoriesJson, new TypeReference<List<String>>() {});

            donationService.insertDonation(insertDonation, categoryNames);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("INVALID_DATA_OR_DB_ERROR");
        }

        return ResponseEntity.ok("ok");
    }


    @GetMapping("/user/role")
    public int getRole(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return 3;
        }

        String userName = userDetails.getUsername();
        return userService.getRoleByUserName(userName);
    }

    @GetMapping("/admin/donationState")
    public ResponseEntity<?> getDonationState(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userName = userDetails.getUsername();
        int role = userService.getRoleByUserName(userName);
        List<DonationState> donationState = donationService.getDonationState();

        if (role == 0){
            return ResponseEntity.ok(donationState);
        }else{
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }
    }

    @PatchMapping("/admin/updateDonationState/{donationNo}")
    public ResponseEntity<?> updateDonationState(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("donationNo") Long no
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userName = userDetails.getUsername();
        int role = userService.getRoleByUserName(userName);



        System.out.println(role);
        if (role == 0){
            boolean isVisible = donationService.getDonationStateByNo(no);
            donationService.updateDonationState(no, isVisible);
            return ResponseEntity.ok("ok");
        }else{
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }
    }

}
