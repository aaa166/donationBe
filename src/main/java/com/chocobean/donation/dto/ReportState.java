package com.chocobean.donation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportState {
    private Long reportNo;
    private Long reporterNo;
    private Long reportedNo;
    private Long adminNo;
    private String reportDetails;
    private String reportStatus;    //P:대기  C:철회    R:완료
    private LocalDate reportDate;
    private Long typeNo;
    private String reportType;  //"payComment"

}
