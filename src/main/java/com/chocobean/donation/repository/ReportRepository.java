package com.chocobean.donation.repository;

import com.chocobean.donation.dto.ReportHistory;
import com.chocobean.donation.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report,Long> {

    @Query("""
    SELECT new com.chocobean.donation.dto.ReportHistory(
        r.reportNo, r.reporterNo, r.reportedNo, r.adminNo,
        r.reportDetails, r.reportStatus, r.reportDate, r.payNo
    )
    FROM Report r
    WHERE r.reportedNo = :userNo
      AND r.reportStatus = 'R'
    """)
    List<ReportHistory> findByReportedNo(@Param("userNo") Long userNo);
}
