package com.chocobean.donation.repository;

import com.chocobean.donation.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {


    @Query("SELECT p " +
            "FROM Payment p " +
            "JOIN FETCH p.donation d " +
            "WHERE p.user.userNo = :userNo")
    List<Payment> findPaymentsWithDonationByUserNo(@Param("userNo") Long userNo);


    @Query("""
            SELECT p
            FROM Payment p
            JOIN FETCH p.user
            WHERE p.donation.donationNo = :donationNo
              AND NOT EXISTS (
                  SELECT 1
                  FROM Report r
                  WHERE r.typeNo = p.payNo
                    AND r.reportType = 'payComment'
                    AND r.reportStatus = 'R'
              )
            ORDER BY p.payDate DESC
            """)
    List<Payment> findPaymentsWithUserByDonationNo(@Param("donationNo") Long donationNo);
}
