package com.chocobean.donation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DonationListDate {
    private long donationNo;
    private String donationTitle;
    private String donationOrganization;
    private int donationPercentage;
    private int donationCurrentAmount;
    private String donationImg;
    private LocalDateTime donationDeadlineDate;

}
