package com.chocobean.donation.repository;

import com.chocobean.donation.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<com.chocobean.donation.entity.Donation, Long> {

    @Query("SELECT d FROM " +
            "Donation d " +
            "JOIN d.categories c" +
            " WHERE c.categoryId = :categoryId")
    List<Donation> findDonationsByCategoryId(@Param("categoryId") Integer categoryId);

    List<Donation> findAllByOrderByDonationDeadlineDateAsc();
}
