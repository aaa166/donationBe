package com.chocobean.donation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DonationList {
    private Long donationNo;
    private String title;
    private String organization;
    private int progress;
    private Long amount;
    private String image;
    private LocalDateTime donationDeadlineDate;




}
