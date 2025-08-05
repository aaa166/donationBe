package com.chocobean.donation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "donation")
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long donationNo;

    @Column(nullable = false, length = 100)
    private String donationTitle;

    @Column(nullable = false, length = 2000)
    private String donationContent;

    @Column(nullable = false)
    private int donationInstitutionCode; //기관코드

    @Column(nullable = false)
    private String donationTarget;
    @Column(nullable = false)
    private int donationTargetPeople;

    @Column(nullable = false)
    private int donationGoalAmount;
    private int donationCurrentAmount = 0;
    @Column(nullable = false)
    private String donationAmountPlan;

    private LocalDateTime donationCreateDate = LocalDateTime.now();
    private LocalDateTime donationDeadlineDate;

    private int[] donationCode;

}
