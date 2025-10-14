package com.chocobean.donation.repository;

import com.chocobean.donation.dto.MyDonation;
import com.chocobean.donation.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PayRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT new com.chocobean.donation.dto.MyDonation(d.donationTitle, p.payAmount, p.payDate) " +
            "FROM Payment p " + // <--- 실제 엔티티 클래스 이름(Payment)으로 수정
            "JOIN p.donation d " +
            "JOIN p.user u " +
            "WHERE u.userNo = :userNo")
    List<MyDonation> findDonationsByUserNo(@Param("userNo") Long userNo);
}