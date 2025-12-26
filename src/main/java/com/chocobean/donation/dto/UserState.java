package com.chocobean.donation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserState {
    private Long userNo;
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private int userRole;
    private Long totalAmount;
    private String userState;


}
