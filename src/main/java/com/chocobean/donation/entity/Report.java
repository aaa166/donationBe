package com.chocobean.donation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "report")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportNo;

    @Column(nullable = false)
    private Long reporterNo;

    @Column(nullable = false)
    private Long reportedNo;

    @Column
    private Long adminNo;

    @Column(nullable = false)
    private String reportDetails;

    @Column(nullable = false)
    private String reportStatus;    //P:대기  C:철회    R:완료

    @Column(nullable = false)
    private LocalDate reportDate = LocalDate.now();

    @Column
    private Long payNo;
}
