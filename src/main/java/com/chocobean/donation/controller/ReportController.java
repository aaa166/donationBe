package com.chocobean.donation.controller;

import com.chocobean.donation.dto.InsertReport;
import com.chocobean.donation.dto.ReportHistory;
import com.chocobean.donation.dto.ReportState;
import com.chocobean.donation.service.ReportService;
import com.chocobean.donation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

        boolean isReported = reportService.getReportStatusByReportData(reportData);
        //신고 여부
        if (isReported){
            return ResponseEntity
                    .status(409) // Conflict (중복 리소스)
                    .body("ALREADY_REPORTED");
        }

        //신고 추가
        reportService.insertReport(userNo,reportData);


        return ResponseEntity.ok("ok");
    }

    @GetMapping("/admin/findReportHistory")
    public ResponseEntity<?> findReportHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("userNo") Long userNo
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userId = userDetails.getUsername();
        int role = userService.getRoleByUserName(userId);

        List<ReportHistory> reportHistories = reportService.findReportHistory(userNo);
        System.out.println(reportHistories);


        if (role == 0){
            return ResponseEntity.ok(reportHistories);
        }else{
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }
    }


    @GetMapping("/admin/findReportState")
    public ResponseEntity<?> findReportState(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userId = userDetails.getUsername();
        int role = userService.getRoleByUserName(userId);

        List<ReportState> reports = reportService.findReports();


        if (role == 0){
            return ResponseEntity.ok(reports);
        }else{
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }
    }

    @GetMapping("/admin/changeReportStateC")
    public ResponseEntity<?> changeReportStateC(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("reportNo") Long reportNo
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userId = userDetails.getUsername();
        int role = userService.getRoleByUserName(userId);


        if (role == 0){
            reportService.changeReportStateC(reportNo,userId);
            return ResponseEntity.ok("ok");
        }else{
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }
    }
    @GetMapping("/admin/changeReportStateR")
    public ResponseEntity<?> changeReportStateR(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("reportNo") Long reportNo
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("UNAUTHORIZED");
        }
        String userId = userDetails.getUsername();
        int role = userService.getRoleByUserName(userId);


        if (role == 0){
            reportService.changeReportStateR(reportNo,userId);
            return ResponseEntity.ok("ok");
        }else{
            return ResponseEntity.status(403).body("NO_PERMISSION");
        }
    }

}

