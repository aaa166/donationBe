package com.chocobean.donation.service;

import com.chocobean.donation.dto.InsertReport;
import com.chocobean.donation.entity.Report;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.ReportRepository;
import com.chocobean.donation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
        report.setPayNo(reportData.getPayNo());

        if (role == 0){
            report.setAdminNo(userNo);
            report.setReportStatus("R");
        }else{
            report.setAdminNo(null);
            report.setReportStatus("P");
        }
        reportRepository.save(report);
    }
}
