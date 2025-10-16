package com.chocobean.donation.repository;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.dto.DonationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<com.chocobean.donation.entity.Donation, Long> {
    @Query("SELECT new com.chocobean.donation.dto.DonationList(d.donationTitle, d.donationOrganization, "
            + "CAST((d.donationCurrentAmount * 100.0 / d.donationGoalAmount) AS int), "
            + "d.donationCurrentAmount, d.donationImg, d.donationDeadlineDate) "
            + "FROM Donation d")
    List<DonationList> findDonationSummaries();


    @Query("SELECT new com.chocobean.donation.dto.DonationList(d.donationTitle, d.donationOrganization, "
            + "CAST((d.donationCurrentAmount * 100.0 / d.donationGoalAmount) AS int), "
            + "d.donationCurrentAmount, d.donationImg, d.donationDeadlineDate) "
            + "FROM Donation d JOIN d.donationCode dc "
            + "WHERE dc = :code")
    List<DonationList> findDonationSummariesByDonationCode(@Param("code") Integer code);

    @Query("SELECT new com.chocobean.donation.dto.DonationList(d.donationTitle, d.donationOrganization, " +
            "CAST((d.donationCurrentAmount * 100.0 / d.donationGoalAmount) AS int), " +
            "d.donationCurrentAmount, d.donationImg, d.donationDeadlineDate) " +
            "FROM Donation d ORDER BY d.donationDeadlineDate ASC")
    List<DonationList> findAllByOrderByDonationDeadlineDateAsc();

    @Query("SELECT new com.chocobean.donation.dto.DonationView(" +
            "d.donationTitle, d.donationTarget, d.donationGoalAmount, d.donationCurrentAmount, " +
            "d.donationAmountPlan, d.donationDeadlineDate, d.donationImg, u.userName, " +
            "p.payAmount, p.payComent, p.payDate) " +
            "FROM Pay p " +
            "JOIN p.donation d " +
            "JOIN p.user u " +
            "WHERE d.donationNo = :donationNo")
    List<DonationView> getDonationByNo(@Param("donationNo")Long no);
}
