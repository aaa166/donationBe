package com.chocobean.donation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class MyDonation {
    private String donationTitle;

    private LocalDate payDate;
    private Long payAmount;

    public MyDonation(String donationTitle, Long payAmount, LocalDate payDate) {
        this.donationTitle = donationTitle;
        this.payAmount = payAmount;
        this.payDate = payDate;
    }

}
