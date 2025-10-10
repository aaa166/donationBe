package com.chocobean.donation.dto;

import com.chocobean.donation.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String userName;
    private String userId;
    private String userEmail;
    private String userPhone;
    private String totalAmount;

    // Entity를 DTO로 변환하는 생성자
    public UserResponse(User user) {
        this.userName = user.getUserName();
        this.userId = user.getUserId();
        this.userEmail = user.getUserEmail();
        this.userPhone = user.getUserPhone();
        this.totalAmount = user.getTotalAmount();
    }
}
