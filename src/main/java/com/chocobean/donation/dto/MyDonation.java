package com.chocobean.donation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MyDonation {
    private String donationTitle;

    private LocalDateTime payDate;
    private Long payAmount;

    public MyDonation(String donationTitle, Long payAmount, LocalDateTime payDate) {
        this.donationTitle = donationTitle;
        this.payAmount = payAmount;
        this.payDate = payDate;
    }

}
