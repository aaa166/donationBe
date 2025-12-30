package com.chocobean.donation.controller;

import com.chocobean.donation.dto.InsertReport;
import com.chocobean.donation.service.ReportService;
import com.chocobean.donation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {
    private final ReportService reportService;
    private final UserService userService;

    @PostMapping("/insertReport")
    public ResponseEntity<?> insertReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody InsertReport reportData
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userId = userDetails.getUsername();
        Long userNo = userService.getUserNoByUserId(userId);
        System.out.println(reportData.getPayNo());
        //신고 여부
//        if (){
//        }

        //신고 추가
        reportService.insertReport(userNo,reportData);


        return ResponseEntity.ok("ok");
    }
}

