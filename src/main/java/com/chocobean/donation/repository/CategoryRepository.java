package com.chocobean.donation.repository;

import com.chocobean.donation.entity.DonationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<DonationCategory, Long> {

    List<DonationCategory> findByCategoryNameIn(List<String> categoryNames);
}
