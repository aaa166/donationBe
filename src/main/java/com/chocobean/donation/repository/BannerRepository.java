package com.chocobean.donation.repository;

import com.chocobean.donation.dto.InsertBanner;
import com.chocobean.donation.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<com.chocobean.donation.entity.Banner, Long>  {

    @Query("SELECT b FROM Banner b " +
            "WHERE b.bannerStartDate <= :today " +
            "AND b.bannerDeadlineDate >= :today")
    List<Banner> findActiveBanners(@Param("today") LocalDate today);
}
