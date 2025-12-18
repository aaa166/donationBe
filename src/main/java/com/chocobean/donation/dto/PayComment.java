package com.chocobean.donation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PayComment {
    private String userName;
    private String payComment;
    private Long payAmount;
    private LocalDate payDate;

}
