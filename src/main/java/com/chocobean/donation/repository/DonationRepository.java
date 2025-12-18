package com.chocobean.donation.repository;

import com.chocobean.donation.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<com.chocobean.donation.entity.Donation, Long> {



    @Query("SELECT d FROM " +
            "Donation d " +
            "JOIN d.categories c" +
            " WHERE c.categoryId = :categoryId")
    List<Donation> findDonationsByCategoryId(@Param("categoryId") Integer categoryId);

    List<Donation> findAllByOrderByDonationDeadlineDateAsc();

    @Query("SELECT d.donationState FROM Donation d WHERE d.donationNo = :donationNo")
    String getDonationStateByDonationNo(@Param("donationNo") Long donationNo);

    @Modifying
    @Query("UPDATE Donation d SET d.donationState = :state WHERE d.donationNo = :no")
    void updateDonationStateByNo(@Param("no") Long no, @Param("state") String state);

    Optional<Donation> findByDonationNo(Long donationNo);

    Donation getDonationByDonationNo(Long donationNo);
}
