package com.chocobean.donation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DonationState {
    private long donationNo;
    private String donationTitle;
    private String donationOrganization;
    private LocalDate donationDeadlineDate;
    private String donationState;

}
