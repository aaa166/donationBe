package com.chocobean.donation.repository;

import com.chocobean.donation.dto.DonationList;
import com.chocobean.donation.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<com.chocobean.donation.entity.Donation, Long> {
    @Query("SELECT new com.chocobean.donation.dto.DonationList(d.donationTitle, d.donationOrganization, " +
            "CAST((d.donationCurrentAmount * 100.0 / d.donationGoalAmount) AS int), " +
            "d.donationCurrentAmount, d.donationImg, d.donationDeadlineDate) " +
            "FROM Donation d")
    List<DonationList> findDonationSummaries();

    @Query("SELECT new com.chocobean.donation.dto.DonationList(d.donationTitle, d.donationOrganization, " +
            "CAST((d.donationCurrentAmount * 100.0 / d.donationGoalAmount) AS int), " +
            "d.donationCurrentAmount, d.donationImg, d.donationDeadlineDate) " +
            "FROM Donation d ORDER BY d.donationDeadlineDate ASC")
    List<DonationList> findAllByOrderByDonationDeadlineDateAsc();
}
