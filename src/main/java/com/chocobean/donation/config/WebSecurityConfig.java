package com.chocobean.donation.config;

import com.chocobean.donation.entity.Provider;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.security.jwt.JwtAuthenticationEntryPoint;
import com.chocobean.donation.security.jwt.JwtRequestFilter;
import com.chocobean.donation.service.CustomOAuth2UserService;
import com.chocobean.donation.service.UserService;
import com.chocobean.donation.utils.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final ApplicationContext context;

    private CustomOAuth2UserService customOAuth2UserService;

    // Lazy 주입으로 순환 참조 방지
    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    public void setCustomOAuth2UserService(@Lazy CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/images/**",
                                "/favicon.ico",
                                "/login/oauth2/code/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // JWT 필터 추가
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

//        // OAuth2 Login
//        http.oauth2Login(oauth2 -> oauth2
//                .userInfoEndpoint(userInfo -> userInfo
//                        .userService(customOAuth2UserService)
//                )
//                .successHandler((request, response, authentication) -> {
//                    // 1. 네이버에서 넘겨준 유저 정보 가져오기
//                    var oAuth2User = (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
//                    var attributes = oAuth2User.getAttributes();
//
//                    // 네이버는 'response'라는 키 안에 실제 정보(id, email 등)가 들어있습니다.
//                    var responseMap = (java.util.Map<String, Object>) attributes.get("response");
//
//                    String providerId = (String) responseMap.get("id");
//                    String email = (String) responseMap.get("email");
//                    String name = (String) responseMap.get("name");
//                    String mobile = (String) responseMap.get("mobile");
//                    String phone = (mobile != null) ? mobile.replace("-", "") : "";
//
//                    UserService userService = context.getBean(UserService.class);
//
//                    User user = userService.socialLogin(providerId, name, email, phone, providerId, Provider.NAVER);
//
//                    // 3. 토큰 생성을 위한 정보 설정
//                    String username = user.getUserId();
//                    String role = (user.getUserRole() == 0) ? "ROLE_ADMIN" : "ROLE_USER";
//
//                    // 4. JWT 토큰 발행
//                    String accessToken = jwtTokenUtil.generateAccessToken(username, role);
//                    String refreshToken = jwtTokenUtil.generateRefreshToken(username);
//
//                    // 5. 프론트엔드 메인 페이지로 리다이렉트 (토큰 포함)
//                    String targetUrl = org.springframework.web.util.UriComponentsBuilder
//                            .fromUriString("http://localhost:5173/")
//                            .queryParam("accessToken", accessToken)
//                            .queryParam("refreshToken", refreshToken)
//                            .build().toUriString();
//
//                    response.sendRedirect(targetUrl);
//                })
//        );
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                )
                .successHandler((request, response, authentication) -> {
                    // 현재 어떤 소셜로 로그인했는지 ID 확인 (google, naver, kakao)
                    String registrationId = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken)authentication).getAuthorizedClientRegistrationId();
                    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                    Map<String, Object> attributes = oAuth2User.getAttributes();

                    User user = null;

                    if ("naver".equals(registrationId)) {
                        Map<String, Object> responseMap = (Map<String, Object>) attributes.get("response");
                        user = userService.socialLogin(
                                (String) responseMap.get("id"),
                                (String) responseMap.get("name"),
                                (String) responseMap.get("email"),
                                ((String) responseMap.get("mobile")).replace("-", ""),
                                (String) responseMap.get("id"),
                                Provider.NAVER
                        );
                    }
                    else if ("kakao".equals(registrationId)) {
                        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                        String providerId = String.valueOf(attributes.get("id"));
                        String nickname = (profile != null) ? (String) profile.get("nickname") : "익명사용자";

                        user = userService.socialLogin(providerId, nickname, null, null, providerId, Provider.KAKAO);
                    }
                    else if ("google".equals(registrationId)) {
                        String providerId = (String) attributes.get("sub");
                        String name = (String) attributes.get("name");
                        String email = (String) attributes.get("email");

                        user = userService.socialLogin(providerId, name, email, null, providerId, Provider.GOOGLE);
                    }

                    // DB에 저장되거나 조회된 유저 정보를 바탕으로 JWT 발급
                    if (user != null) {
                        String username = user.getUserId();
                        String role = (user.getUserRole() == 0) ? "ROLE_ADMIN" : "ROLE_USER";

                        String accessToken = jwtTokenUtil.generateAccessToken(username, role);
                        String refreshToken = jwtTokenUtil.generateRefreshToken(username);

                        String targetUrl = org.springframework.web.util.UriComponentsBuilder
                                .fromUriString("http://localhost:5173/")
                                .queryParam("accessToken", accessToken)
                                .queryParam("refreshToken", refreshToken)
                                .build().toUriString();

                        response.sendRedirect(targetUrl);
                    }
                })
        );


        return http.build();
    }
}
//  localStorage.getItem("accessToken")