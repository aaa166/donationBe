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

//        String reportStatus = reportService.getReportStatusByReportData(reportData);
        //신고 여부
//        if (){
//        }

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

    //하는중
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
}

