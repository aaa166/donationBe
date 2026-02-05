package com.chocobean.donation.service;

import com.chocobean.donation.dto.InsertBanner;
import com.chocobean.donation.entity.Banner;
import com.chocobean.donation.repository.BannerRepository;
import com.chocobean.donation.repository.DonationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {
    private final BannerRepository bannerRepository;

    @Transactional
    public List<InsertBanner> findAll() {
        List<Banner> bannerEntities = bannerRepository.findAll();
        List<InsertBanner> banners = bannerEntities.stream()
                .map(b -> new InsertBanner(b))
                .collect(Collectors.toList());
        return banners;
    }

    @Transactional
    public void insertBanner(InsertBanner insertBanner) {
        Banner banner = new Banner();
        banner.setBannerTitle(insertBanner.getBannerTitle());
        banner.setBannerURL(insertBanner.getBannerURL());
        banner.setBannerImg(insertBanner.getBannerImg());
        banner.setBannerStartDate(insertBanner.getBannerStartDate());
        banner.setBannerDeadlineDate(insertBanner.getBannerDeadlineDate());

        bannerRepository.save(banner);
    }

    @Transactional
    public List<InsertBanner> findActiveBanners() {
        LocalDate today = LocalDate.now();
        List<Banner> banners = bannerRepository.findActiveBanners(today);
        List<InsertBanner> activeBanners = banners.stream()
                .map(InsertBanner::new)
                .collect(Collectors.toList());

        return activeBanners;
    }
}
