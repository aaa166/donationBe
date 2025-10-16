package com.chocobean.donation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonationView {
    private String donationTitle;
    private String donationTarget;
    private int donationGoalAmount;
    private int donationCurrentAmount;
    private String donationAmountPlan;
    private LocalDateTime donationDeadlineDate;
    private String donationImg;

    private Long payAmount;
    private String payComent;
    private LocalDateTime payDate;

    private String userName;


}
