package com.chocobean.donation.dto;

import com.chocobean.donation.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userNo;
    private String userName;
    private String userId;
    private String userEmail;
    private String userPhone;
    private Long totalAmount;
    private int userRole;

    // Entity를 DTO로 변환하는 생성자
    public UserResponse(User user) {
        this.userNo = user.getUserNo();
        this.userName = user.getUserName();
        this.userId = user.getUserId();
        this.userEmail = user.getUserEmail();
        this.userPhone = user.getUserPhone();
        this.totalAmount = user.getTotalAmount();
        this.userRole = user.getUserRole();
    }
}
