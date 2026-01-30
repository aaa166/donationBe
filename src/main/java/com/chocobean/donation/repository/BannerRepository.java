package com.chocobean.donation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerRepository extends JpaRepository<com.chocobean.donation.entity.Banner, Long>  {

}
