package com.chocobean.donation.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SignUpForm {
    private String userName;
    private String userId;
    private String userPassword;
    private String userEmail;
    private String userPhone;
}
