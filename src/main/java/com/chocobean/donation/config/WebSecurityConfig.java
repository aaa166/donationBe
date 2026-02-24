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
                                "/images/**"
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
                    // 1. 네이버에서 넘겨준 유저 정보 가져오기
                    var oAuth2User = (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();
                    var attributes = oAuth2User.getAttributes();

                    // 네이버는 'response'라는 키 안에 실제 정보(id, email 등)가 들어있습니다.
                    var responseMap = (java.util.Map<String, Object>) attributes.get("response");

                    String providerId = (String) responseMap.get("id");
                    String email = (String) responseMap.get("email");
                    String name = (String) responseMap.get("name");
                    String mobile = (String) responseMap.get("mobile");
                    String phone = (mobile != null) ? mobile.replace("-", "") : "";

                    // 2. 서비스 레이어 호출 (ApplicationContext를 통해 Bean 가져오기 - 순환참조 방지)
                    UserService userService = context.getBean(UserService.class);

                    // [핵심] socialLogin 메서드는 유저가 없으면 DB에 저장(가입)하고, 있으면 조회해서 가져옵니다.
                    User user = userService.socialLogin(providerId, name, email, phone, providerId, Provider.NAVER);

                    // 3. 토큰 생성을 위한 정보 설정
                    String username = user.getUserId();
                    String role = (user.getUserRole() == 0) ? "ROLE_ADMIN" : "ROLE_USER";

                    // 4. JWT 토큰 발행
                    String accessToken = jwtTokenUtil.generateAccessToken(username, role);
                    String refreshToken = jwtTokenUtil.generateRefreshToken(username);

                    // 5. 프론트엔드 메인 페이지로 리다이렉트 (토큰 포함)
                    String targetUrl = org.springframework.web.util.UriComponentsBuilder
                            .fromUriString("http://localhost:5173/")
                            .queryParam("accessToken", accessToken)
                            .queryParam("refreshToken", refreshToken)
                            .build().toUriString();

                    response.sendRedirect(targetUrl);
                })
        );

        return http.build();
    }
}
//  localStorage.getItem("accessToken")