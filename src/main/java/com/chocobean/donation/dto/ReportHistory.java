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
public class ReportHistory {
    private Long reportNo;
    private Long reporterNo;
    private Long reportedNo;
    private Long adminNo;
    private String reportDetails;
    private String reportStatus;
    private LocalDate reportDate;
    private String reportType;  //"payComment"
    private Long typeNo;

}
