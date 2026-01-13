package com.chocobean.donation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportState {
    private Long reportNo;
    private String reporterId;
    private String reportedId;
    private String adminNo;
    private String reportDetails;
    private String reportStatus;
    private LocalDate reportDate;
    private String reportType;  //"payComment"
    private Long typeNo;
    private Long donationNo;

}
