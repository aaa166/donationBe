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

            String id = (String) response.get("id");
            String name = (String) response.get("name");
            String email = (String) response.get("email");
            String providerId = (String) response.get("id");
            String phone = ((String) response.get("mobile")).replace("-", "");

            // UserService 호출: DB 조회/가입 처리
            User user = userService.socialLogin(id, name, email, phone, providerId,  Provider.NAVER);


        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String id = String.valueOf(attributes.get("id"));

            // 계정 정보가 없을 경우를 대비한 방어 코드
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            String nickname = "익명사용자";

            if (kakaoAccount != null) {
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null && profile.get("nickname") != null) {
                    nickname = (String) profile.get("nickname");
                }
            }

            userService.socialLogin(id, nickname, null, null, id, Provider.KAKAO);
        }

        return oAuth2User;
    }
}