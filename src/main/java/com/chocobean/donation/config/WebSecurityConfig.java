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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final JwtTokenUtil jwtTokenUtil;

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
                                "/oauth2/**"
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

        // OAuth2 Login
        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                )
                .successHandler((request, response, authentication) -> {
                    // OAuth2User 정보 가져오기
                    var oAuth2User = (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
                    var attributes = oAuth2User.getAttributes();

                    // providerId, Provider 추출 (네이버 예시)
                    var responseMap = (java.util.Map<String, Object>) attributes.get("response");
                    String providerId = (String) responseMap.get("id");
                    Provider provider = Provider.NAVER;

                    // DB에서 User 가져오기
                    User user = userService.findByProviderAndProviderId(provider, providerId)
                            .orElseThrow(() -> new IllegalStateException("사용자 없음"));

                    // JWT 토큰 생성
                    String username = user.getUserId();
                    String role = user.getUserRole() == 0 ? "ROLE_ADMIN" : "ROLE_USER";

                    String accessToken = jwtTokenUtil.generateAccessToken(username, role);
                    String refreshToken = jwtTokenUtil.generateRefreshToken(username);

                    // React 프론트로 리다이렉트 + 토큰 전달
                    String redirectUrl = "http://localhost:5173/oauth2/callback?accessToken=" + accessToken
                            + "&refreshToken=" + refreshToken;

                    response.sendRedirect(redirectUrl);
                })
        );

        return http.build();
    }
}