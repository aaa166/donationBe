package com.chocobean.donation.integration;

import com.chocobean.donation.dto.InsertReport;
import com.chocobean.donation.dto.ReportHistory;
import com.chocobean.donation.dto.ReportState;
import com.chocobean.donation.entity.Report;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.ReportRepository;
import com.chocobean.donation.repository.UserRepository;
import com.chocobean.donation.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ReportService 통합 테스트")
class ReportServiceIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 신고등록_및_상태변경_통합검증() {
        // Given: 신고자(일반 유저)와 어드민 계정, 그리고 신고 대상 세팅
        User reporter = new User();
        reporter.setUserId("reporter");
        reporter.setUserRole(1); // 일반 유저
        User savedReporter = userRepository.save(reporter);

        User admin = new User();
        admin.setUserId("admin_user");
        admin.setUserRole(0); // 관리자
        User savedAdmin = userRepository.save(admin);

        InsertReport reportDto = new InsertReport();
        reportDto.setReportedId(99L);
        reportDto.setReportDetails("부적절한 내용입니다.");
        reportDto.setReportType("D");
        reportDto.setTypeNo(100L);

        // When: 신고 등록
        reportService.insertReport(savedReporter.getUserNo(), reportDto);

        // Then: DB에 대기 상태(P)로 저장됨
        List<ReportState> allReports = reportService.findReports();
        assertThat(allReports).isNotEmpty();
        
        ReportState savedReport = allReports.stream()
                .filter(r -> r.getReportDetails().equals("부적절한 내용입니다."))
                .findFirst()
                .orElseThrow();
        
        assertThat(savedReport.getReportStatus()).isEqualTo("P");

        // When: 어드민이 해당 신고를 승인(C) 처리
        reportService.changeReportStateC(savedReport.getReportNo(), savedAdmin.getUserId());

        // Then: 상태가 C로 변경되고 adminNo가 매핑됨
        Report updatedReport = reportRepository.findById(savedReport.getReportNo()).orElseThrow();
        assertThat(updatedReport.getReportStatus()).isEqualTo("C");
        assertThat(updatedReport.getAdminNo()).isEqualTo(savedAdmin.getUserNo());

        // When: 신고 히스토리 조회
        List<ReportHistory> history = reportService.findReportHistory(99L);
        
        // Then: 히스토리 목록 반환
        assertThat(history).isNotEmpty();
        assertThat(history.get(0).getReportStatus()).isEqualTo("C");
    }
}
