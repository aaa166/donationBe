package com.chocobean.donation.service;

import com.chocobean.donation.dto.InsertReport;
import com.chocobean.donation.dto.ReportHistory;
import com.chocobean.donation.entity.Report;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.ReportRepository;
import com.chocobean.donation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    public List<ReportHistory> findReportHistory(Long userNo) {
        return reportRepository.findByReportedNo(userNo);
    }

//    @Transactional
//    public String getReportStatusByReportData(InsertReport reportData) {
//        Long payNo = reportData.getPayNo();
//        return reportRepository.getReportStatusBy
//    }
}
