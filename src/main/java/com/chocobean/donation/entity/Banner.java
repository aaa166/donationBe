package com.chocobean.donation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "banner")
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long bannerNo;
    @Column(nullable = false)
    private String bannerTitle;
    @Column(nullable = false)
    private String bannerImg;
    @Column(nullable = false)
    private String bannerURL;
    @Column(nullable = false)
    private LocalDate bannerCreateDate = LocalDate.now();
    @Column(nullable = false)
    private LocalDate bannerStartDate;
    @Column(nullable = false)
    private LocalDate bannerDeadlineDate;

}
