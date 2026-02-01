package com.chocobean.donation.dto;

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
}
