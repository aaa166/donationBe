package com.chocobean.donation.repository;

import com.chocobean.donation.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {


    @Query("SELECT p " +
            "FROM Payment p " +
            "JOIN FETCH p.donation d " +
            "WHERE p.user.userNo = :userNo")
    List<Payment> findPaymentsWithDonationByUserNo(@Param("userNo") Long userNo);


//    @Query("SELECT new com.chocobean.donation.dto.PayComment(u.userName, p.payComment, p.payAmount, p.payDate) " +
//            "FROM Payment p " +
//            "JOIN p.user u " +
//            "WHERE p.donation.donationNo = :donationNo")
//    List<PayComment> findPayCommentsByDonationNo(@Param("donationNo") Long donationNo);
    @Query("SELECT p " +
            "FROM Payment p " +
            "JOIN FETCH p.user u " +
            "WHERE p.donation.donationNo = :donationNo")
    List<Payment> findPaymentsWithUserByDonationNo(@Param("donationNo") Long donationNo);
}