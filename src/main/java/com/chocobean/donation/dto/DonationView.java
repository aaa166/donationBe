package com.chocobean.donation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // ★★★ Setter 추가

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
// @AllArgsConstructor // <- 제거
public class DonationView {
    private String donationTitle;
    private String donationContent;
    private int donationInstitutionCode;
    private String donationOrganization;
    private String donationTarget;
    private int donationTargetPeople;
    private int donationGoalAmount;
    private int donationCurrentAmount;
    private String donationAmountPlan;
    private LocalDateTime donationCreateDate;
    private LocalDateTime donationDeadlineDate;
    private String donationImg;

    @Setter // ★★★ List 필드에만 Setter를 추가
    private List<Integer> donationCode;

    // ★★★ JPQL에서 사용할 생성자 (List 제외) ★★★
    public DonationView(String donationTitle, String donationContent, int donationInstitutionCode,
                        String donationOrganization, String donationTarget, int donationTargetPeople,
                        int donationGoalAmount, int donationCurrentAmount, String donationAmountPlan,
                        LocalDateTime donationCreateDate, LocalDateTime donationDeadlineDate, String donationImg) {
        this.donationTitle = donationTitle;
        this.donationContent = donationContent;
        this.donationInstitutionCode = donationInstitutionCode;
        this.donationOrganization = donationOrganization;
        this.donationTarget = donationTarget;
        this.donationTargetPeople = donationTargetPeople;
        this.donationGoalAmount = donationGoalAmount;
        this.donationCurrentAmount = donationCurrentAmount;
        this.donationAmountPlan = donationAmountPlan;
        this.donationCreateDate = donationCreateDate;
        this.donationDeadlineDate = donationDeadlineDate;
        this.donationImg = donationImg;
    }
}