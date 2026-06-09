package com.chocobean.donation.service;

import com.chocobean.donation.dto.InsertReport;
import com.chocobean.donation.dto.ReportHistory;
import com.chocobean.donation.dto.ReportState;
import com.chocobean.donation.entity.Report;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.ReportRepository;
import com.chocobean.donation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService 단위 테스트")
class ReportServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    @Nested
    @DisplayName("insertReport 메서드는")
    class InsertReportTest {

        private InsertReport createInsertReportDto() {
            InsertReport dto = new InsertReport();
            dto.setReportedId(20L); // 신고 대상자
            dto.setReportDetails("부적절한 게시글 내용입니다.");
            dto.setReportType("D"); // 기부(Donation) 관련 신고 등
            dto.setTypeNo(100L);
            return dto;
        }

        @Test
        @DisplayName("성공: 일반 사용자(role != 0)가 신고하면 대기 상태('P')로 저장된다")
        void success_insertReport_byNormalUser() {
            // Given
            Long reporterNo = 10L;
            InsertReport reportData = createInsertReportDto();

            User reporter = new User();
            reporter.setUserNo(reporterNo);
            reporter.setUserRole(1); // 일반 유저 (role != 0)

            given(userRepository.findByUserNo(reporterNo)).willReturn(Optional.of(reporter));

            // ArgumentCaptor를 사용하여 실제 저장되는 Report 엔티티 검증
            ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);

            // When
            reportService.insertReport(reporterNo, reportData);

            // Then
            verify(reportRepository, times(1)).save(reportCaptor.capture());
            Report savedReport = reportCaptor.getValue();

            assertThat(savedReport.getReporterNo()).isEqualTo(reporterNo);
            assertThat(savedReport.getReportedNo()).isEqualTo(20L);
            assertThat(savedReport.getReportDetails()).isEqualTo("부적절한 게시글 내용입니다.");
            assertThat(savedReport.getReportStatus()).isEqualTo("P"); // 대기 상태
            assertThat(savedReport.getAdminNo()).isNull(); // 일반 사용자 신고이므로 adminNo는 null
            assertThat(savedReport.getReportDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("성공: 관리자(role == 0)가 신고하면 즉시 승인/처리 상태('R') 및 어드민 번호가 지정되어 저장된다")
        void success_insertReport_byAdmin() {
            // Given
            Long adminNo = 1L;
            InsertReport reportData = createInsertReportDto();

            User admin = new User();
            admin.setUserNo(adminNo);
            admin.setUserRole(0); // 관리자 (role == 0)

            given(userRepository.findByUserNo(adminNo)).willReturn(Optional.of(admin));

            ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);

            // When
            reportService.insertReport(adminNo, reportData);

            // Then
            verify(reportRepository, times(1)).save(reportCaptor.capture());
            Report savedReport = reportCaptor.getValue();

            assertThat(savedReport.getReporterNo()).isEqualTo(adminNo);
            assertThat(savedReport.getReportStatus()).isEqualTo("R"); // 관리자 신고이므로 즉시 처리
            assertThat(savedReport.getAdminNo()).isEqualTo(adminNo); // 처리 어드민 번호 매핑
        }

        @Test
        @DisplayName("실패: 신고 주체인 유저 번호가 존재하지 않는다면 EntityNotFoundException을 던진다")
        void fail_whenUserNotFound() {
            // Given
            Long nonExistentUserNo = 999L;
            InsertReport reportData = createInsertReportDto();

            given(userRepository.findByUserNo(nonExistentUserNo)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> reportService.insertReport(nonExistentUserNo, reportData))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage("존재하지 않는 유저입니다.");

            verify(reportRepository, never()).save(any(Report.class));
        }
    }

    @Nested
    @DisplayName("findReportHistory 메서드는")
    class FindReportHistory {

        @Test
        @DisplayName("신고 대상 유저 번호로 해당 유저의 처리 완료(반려) 신고 히스토리 목록을 반환한다")
        void success_findReportHistory() {
            // Given
            Long userNo = 20L;
            ReportHistory history = new ReportHistory(1L, 10L, 20L, 1L, "신고내용", "R", LocalDate.now(), "D", 100L);
            given(reportRepository.findByReportedNo(userNo)).willReturn(List.of(history));

            // When
            List<ReportHistory> result = reportService.findReportHistory(userNo);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getReportNo()).isEqualTo(1L);
            verify(reportRepository, times(1)).findByReportedNo(userNo);
        }
    }

    @Nested
    @DisplayName("findReports 메서드는")
    class FindReports {

        @Test
        @DisplayName("전체 신고 건들에 대한 상태 정보 목록(ReportState DTO)을 반환한다")
        void success_findReports() {
            // Given
            ReportState state = new ReportState(1L, "reporterId", "reportedId", "adminId", "상세내용", "P", LocalDate.now(), "D", 100L, 50L);
            given(reportRepository.findAllReportStates()).willReturn(List.of(state));

            // When
            List<ReportState> result = reportService.findReports();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getReportNo()).isEqualTo(1L);
            verify(reportRepository, times(1)).findAllReportStates();
        }
    }

    @Nested
    @DisplayName("getReportStatusByReportData 메서드는")
    class GetReportStatusByReportData {

        @Test
        @DisplayName("특정 타입 및 유형 번호로 이미 등록된 신고가 존재하는지 여부를 조회하여 반환한다")
        void success_getReportStatusByReportData() {
            // Given
            InsertReport reportData = new InsertReport();
            reportData.setTypeNo(100L);
            reportData.setReportType("D");

            given(reportRepository.existsByTypeNoAndReportType(100L, "D")).willReturn(true);

            // When
            boolean result = reportService.getReportStatusByReportData(reportData);

            // Then
            assertThat(result).isTrue();
            verify(reportRepository, times(1)).existsByTypeNoAndReportType(100L, "D");
        }
    }

    @Nested
    @DisplayName("changeReportStateC 메서드는")
    class ChangeReportStateC {

        @Test
        @DisplayName("어드민 ID로 번호를 조회하여 해당 신고를 승인(C) 상태로 업데이트한다")
        void success_changeReportStateC() {
            // Given
            Long reportNo = 1L;
            String adminId = "admin123";
            Long adminNo = 5L;

            given(userRepository.getUserNoByUserId(adminId)).willReturn(adminNo);

            // When
            reportService.changeReportStateC(reportNo, adminId);

            // Then
            verify(userRepository, times(1)).getUserNoByUserId(adminId);
            verify(reportRepository, times(1)).updateReportStatusToC(reportNo, adminNo);
        }
    }

    @Nested
    @DisplayName("changeReportStateR 메서드는")
    class ChangeReportStateR {

        @Test
        @DisplayName("어드민 ID로 번호를 조회하여 해당 신고를 반려(R) 상태로 업데이트한다")
        void success_changeReportStateR() {
            // Given
            Long reportNo = 1L;
            String adminId = "admin123";
            Long adminNo = 5L;

            given(userRepository.getUserNoByUserId(adminId)).willReturn(adminNo);

            // When
            reportService.changeReportStateR(reportNo, adminId);

            // Then
            verify(userRepository, times(1)).getUserNoByUserId(adminId);
            verify(reportRepository, times(1)).updateReportStatusToR(reportNo, adminNo);
        }
    }
}
