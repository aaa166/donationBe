package com.chocobean.donation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DonationList {
    private String title;
    private String organization;
    private int progress;
    private long amount;
    private String image;


}
