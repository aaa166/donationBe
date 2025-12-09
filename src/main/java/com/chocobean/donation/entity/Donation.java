package com.chocobean.donation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false, length = 4000)
    private String donationContent;

    @Column(nullable = false, length = 100)
    private String donationOrganization; //기관명

    @Column(nullable = false)
    private String donationTarget;
    @Column(nullable = false)
    private int donationTargetCount;
    @Column(nullable = false)
    private int donationGoalAmount;
    private int donationCurrentAmount = 0;
    @Column(nullable = false)
    private String donationPlan;

//    private LocalDateTime donationCreateDate = LocalDateTime.now();
    private LocalDate donationCreateDate = LocalDate.now();
//    private LocalDateTime donationDeadlineDate;
    private LocalDate donationDeadlineDate;
    @Column(nullable = false)
    private String donationImg;

    @Column(nullable = false)
    private String donationState;//    P:대기    A: 게시   D:비활성화

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "donation_to_category",
            joinColumns = @JoinColumn(name = "donation_no"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<DonationCategory> categories = new ArrayList<>();



    @OneToMany(mappedBy = "donation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();



}
