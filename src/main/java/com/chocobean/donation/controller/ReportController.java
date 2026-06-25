package com.chocobean.donation.controller;

import com.chocobean.donation.dto.InsertReport;
import com.chocobean.donation.dto.ReportHistory;
import com.chocobean.donation.dto.ReportState;
import com.chocobean.donation.service.ReportService;
import com.chocobean.donation.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
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
        List<ReportHistory> reportHistories = reportService.findReportHistory(userNo);
        log.info("reportHistories: {}", reportHistories);

        return ResponseEntity.ok(reportHistories);
    }

    //report페이지
    @GetMapping("/admin/findReportState")
    public ResponseEntity<?> findReportState(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ReportState> reports = reportService.findReports();

        return ResponseEntity.ok(reports);
    }

    @PostMapping("/admin/changeReportStateC")
    public ResponseEntity<?> changeReportStateC(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Long> body
    ) {
        String userId = userDetails.getUsername();
        Long reportNo = body.get("reportNo");
        reportService.changeReportStateC(reportNo, userId);
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/admin/changeReportStateR")
    public ResponseEntity<?> changeReportStateR(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Long> body
    ) {
        String userId = userDetails.getUsername();
        Long reportNo = body.get("reportNo");
        reportService.changeReportStateR(reportNo,userId);
        return ResponseEntity.ok("ok");
    }

}

