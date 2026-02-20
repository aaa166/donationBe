package com.chocobean.donation.service;

import com.chocobean.donation.entity.Provider;
import com.chocobean.donation.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 네이버 로그인 처리
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");

            String name = (String) response.get("name");
            String email = (String) response.get("email");
            String providerId = (String) response.get("id");
            String phone = (String) response.get("mobile");

            // UserService 호출: DB 조회/가입 처리
            User user = userService.socialLogin(name, email, phone, providerId,  Provider.NAVER);


        }

        return oAuth2User;
    }
}