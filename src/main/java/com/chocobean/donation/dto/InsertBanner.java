package com.chocobean.donation.dto;

import com.chocobean.donation.entity.Banner;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsertBanner {
    Long bannerNo;
    private String bannerTitle;
    private String bannerImg;
    private String bannerURL;
    private LocalDate bannerCreateDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate bannerStartDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate bannerDeadlineDate;


    public InsertBanner(Banner banner) {
        this.bannerNo = banner.getBannerNo();
        this.bannerTitle = banner.getBannerTitle();
        this.bannerURL = banner.getBannerURL();
        this.bannerImg = banner.getBannerImg();
        this.bannerCreateDate = banner.getBannerCreateDate();
        this.bannerStartDate = banner.getBannerStartDate();
        this.bannerDeadlineDate = banner.getBannerDeadlineDate();
    }
}
