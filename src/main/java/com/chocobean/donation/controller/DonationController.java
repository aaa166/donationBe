package com.chocobean.donation.controller;

import com.chocobean.donation.dto.DonationList;
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

import java.util.List;

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
        String userName = userDetails.getUsername();
        int role = userService.getRoleByUserName(userName);
        System.out.println("userName :"+ userName);
        System.out.println("role :"+ role);
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
            @RequestParam("categories") String categoriesJson // 카테고리 JSON 문자열을 받음
    ) {
        // 1. 파일 처리 로직
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = image.getOriginalFilename();
            } catch (Exception e) {
                System.err.println("파일 저장 오류: " + e.getMessage());
                return ResponseEntity.internalServerError().body("FILE_UPLOAD_FAILED");
            }
        }
        insertDonation.setDonationImg(imageUrl);

        // 2. 카테고리 JSON 파싱 및 서비스 호출
        try {
            ObjectMapper mapper = new ObjectMapper();

            // JSON 문자열을 카테고리 이름 목록(String List)으로 파싱
            List<String> categoryNames = mapper.readValue(categoriesJson, new TypeReference<List<String>>() {});

            // 💡 핵심 수정: 서비스 호출 변경
            // DTO와 카테고리 이름 목록을 서비스로 전달하여 단일 트랜잭션 내에서 처리하도록 위임합니다.
            // 기존의 donationService.getCategoryEntitiesByNames() 호출 제거

            donationService.insertDonation(insertDonation, categoryNames);

        } catch (Exception e) {
            // 🚨 디버깅을 위해 에러 로그를 출력하고, 프론트엔드에 400 Bad Request를 반환합니다.
            System.err.println("캠페인 등록 중 오류 발생 (JSON 파싱 또는 DB 문제): " + e.getMessage());
            e.printStackTrace(); // 자세한 오류 추적을 위해 추가
            return ResponseEntity.badRequest().body("INVALID_DATA_OR_DB_ERROR");
        }

        System.out.println(insertDonation.getDonationTitle());
        System.out.println(insertDonation.getDonationImg());

        return ResponseEntity.ok("ok");
    }


}
