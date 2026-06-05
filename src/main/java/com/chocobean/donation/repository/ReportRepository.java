package com.chocobean.donation.repository;

import com.chocobean.donation.dto.ReportHistory;
import com.chocobean.donation.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report,Long> {

    @Query("""
    SELECT new com.chocobean.donation.dto.ReportHistory(
        r.reportNo, r.reporterNo, r.reportedNo, r.adminNo,
        r.reportDetails, r.reportStatus, r.reportDate, r.reportType,r.typeNo
    )
    FROM Report r
    WHERE r.reportedNo = :userNo
      AND r.reportStatus = 'R'
    """)
    List<ReportHistory> findByReportedNo(@Param("userNo") Long userNo);


    boolean existsByTypeNoAndReportType(Long typeNo, String reportType);

    @Modifying
    @Query("""
        UPDATE Report r
        SET r.reportStatus = 'C',
            r.adminNo = :adminNo
        WHERE r.reportNo = :reportNo
    """)
    void updateReportStatusToC(
            @Param("reportNo") Long reportNo,
            @Param("adminNo") Long adminNo
    );

    @Modifying
    @Query("""
        UPDATE Report r
        SET r.reportStatus = 'R',
            r.adminNo = :adminNo
        WHERE r.reportNo = :reportNo
    """)
    void updateReportStatusToR(
            @Param("reportNo") Long reportNo,
            @Param("adminNo") Long adminNo
    );

    @Query("SELECT p.donation.donationNo FROM Payment p WHERE p.payNo = :typeNo")
    Long findDonationNoByTypeNo(@Param("typeNo") Long typeNo);

    @Query("""
        SELECT new com.chocobean.donation.dto.ReportState(
            r.reportNo,
            ru.userId,
            rpu.userId,
            au.userId,
            r.reportDetails,
            r.reportStatus,
            r.reportDate,
            r.reportType,
            r.typeNo,
            d.donationNo
        )
        FROM Report r
        LEFT JOIN User ru ON ru.userNo = r.reporterNo
        LEFT JOIN User rpu ON rpu.userNo = r.reportedNo
        LEFT JOIN User au ON au.userNo = r.adminNo
        LEFT JOIN Payment p ON p.payNo = r.typeNo
        LEFT JOIN Donation d ON d.donationNo = p.donation.donationNo
    """)
    List<ReportState> findAllReportStates();
}
