package com.chocobean.donation.dto;

import com.chocobean.donation.entity.DonationCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InsertDonation {
    private long donationNo;
    private String donationTitle;
    private String donationContent;
    private String donationOrganization;
    private String donationTarget;
    private int donationTargetCount;
    private Long donationGoalAmount;
    private String donationPlan;
    private String donationImg;
//    private LocalDateTime donationDeadlineDate;
    private LocalDate donationDeadlineDate;
//    private List<DonationCategory> categories = new ArrayList<>();
private List<DonationCategory> categoryEntities = new ArrayList<>();

}
