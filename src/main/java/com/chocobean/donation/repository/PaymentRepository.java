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


    @Query(value = "SELECT p.* " +
            "FROM payment p " +
            "LEFT JOIN report r ON r.pay_no = p.pay_no " +
            "WHERE p.donation_no = :donationNo " +
            "AND (r.report_status IS NULL OR r.report_status <> 'R') " +
            "ORDER BY p.pay_date DESC", nativeQuery = true)
    List<Payment> findPaymentsWithUserByDonationNo(@Param("donationNo") Long donationNo);
}