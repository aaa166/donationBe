package com.chocobean.donation.dto;

import com.chocobean.donation.entity.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Oauth2User {
    private String userName;

    private String userEmail;

    private String userPhone;


}
