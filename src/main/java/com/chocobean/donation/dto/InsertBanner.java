package com.chocobean.donation.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private LocalDate bannerStartDate;
    private LocalDate bannerDeadlineDate;
}
