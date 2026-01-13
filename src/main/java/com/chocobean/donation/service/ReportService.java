package com.chocobean.donation.service;

import com.chocobean.donation.dto.InsertReport;
import com.chocobean.donation.dto.ReportHistory;
import com.chocobean.donation.dto.ReportState;
import com.chocobean.donation.entity.Report;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.ReportRepository;
import com.chocobean.donation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;


    @Transactional
    public void insertReport(Long userNo, InsertReport reportData) {
        Report report = new Report();

        User reporter = userRepository.findByUserNo(userNo);
        int role = reporter.getUserRole();

        report.setReporterNo(userNo);
        report.setReportedNo(reportData.getReportedId());
        report.setReportDetails(reportData.getReportDetails());
        report.setReportDate(LocalDate.now());
        report.setReportType(reportData.getReportType());
        report.setTypeNo(reportData.getTypeNo());

        if (role == 0){
            report.setAdminNo(userNo);
            report.setReportStatus("R");
        }else{
            report.setAdminNo(null);
            report.setReportStatus("P");
        }
        reportRepository.save(report);
    }
    @Transactional
    public List<ReportHistory> findReportHistory(Long userNo) {
        return reportRepository.findByReportedNo(userNo);
    }
    @Transactional
    public List<ReportState> findReports() {

        return reportRepository.findAll().stream()
                .map(report -> {
                    String reporterId = userRepository.findUserIdByUserNo(report.getReporterNo());
                    String reportedId = userRepository.findUserIdByUserNo(report.getReportedNo());
                    String adminId = report.getAdminNo() != null ? userRepository.findUserIdByUserNo(report.getAdminNo()) : null;

//                    System.out.println(reporterId);
//                    System.out.println(reportedId);
//                    System.out.println(adminId);
                    return new ReportState(
                            report.getReportNo(),
                            reporterId,
                            reportedId,
                            adminId,
                            report.getReportDetails(),
                            report.getReportStatus(),
                            report.getReportDate(),
                            report.getReportType(),
                            report.getTypeNo()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean getReportStatusByReportData(InsertReport reportData) {
        Long typeNo = reportData.getTypeNo();
        String reportType = reportData.getReportType();
        return reportRepository.existsByTypeNoAndReportType(typeNo, reportType);
    }
    @Transactional
    public void changeReportStateC(Long reportNo,String userId) {
        Long adminNo = userRepository.getUserNoByUserId(userId);
        reportRepository.updateReportStatusToC(reportNo,adminNo);
    }
    @Transactional
    public void changeReportStateR(Long reportNo,String userId) {
        Long adminNo = userRepository.getUserNoByUserId(userId);
        reportRepository.updateReportStatusToR(reportNo,adminNo);
    }
}
