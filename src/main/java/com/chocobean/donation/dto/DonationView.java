package com.chocobean.donation.dto;

import com.chocobean.donation.entity.DonationCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
// @AllArgsConstructor // <- 제거
public class DonationView {
    private String donationTitle;
    private String donationContent;
    private String donationOrganization;
    private String donationTarget;
    private int donationTargetCount;
    private int donationGoalAmount;
    private int donationCurrentAmount;
    private String donationPlan;
    private LocalDateTime donationCreateDate;
    private LocalDateTime donationDeadlineDate;
    private String donationImg;

    private List<DonationCategory> categories = new ArrayList<>();

    // ★★★ JPQL에서 사용할 생성자 (List 제외) ★★★
    public DonationView(String donationTitle, String donationContent,
                        String donationOrganization, String donationTarget, int donationTargetCount,
                        int donationGoalAmount, int donationCurrentAmount, String donationPlan,
                        LocalDateTime donationCreateDate, LocalDateTime donationDeadlineDate, String donationImg) {
        this.donationTitle = donationTitle;
        this.donationContent = donationContent;
        this.donationOrganization = donationOrganization;
        this.donationTarget = donationTarget;
        this.donationTargetCount = donationTargetCount;
        this.donationGoalAmount = donationGoalAmount;
        this.donationCurrentAmount = donationCurrentAmount;
        this.donationPlan = donationPlan;
        this.donationCreateDate = donationCreateDate;
        this.donationDeadlineDate = donationDeadlineDate;
        this.donationImg = donationImg;
    }
}