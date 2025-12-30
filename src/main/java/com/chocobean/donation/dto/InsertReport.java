package com.chocobean.donation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsertReport {
    private Long reporterId;
    private Long reportedId;
    private Long adminId;
    private String reportDetails;
    private String reportStatus;
    private LocalDate reportDate;
    private Long payNo;
}
