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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
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

        @Value("${cors.allowed-origin:http://localhost:5173}")
        private String allowedOrigin;

        private CustomOAuth2UserService customOAuth2UserService;

        @Autowired
        private RedisTemplate<String, String> redisTemplate;

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
                                                                "/login/oauth2/code/**")
                                                .permitAll()
                                                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                // JWT 필터 추가
                http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

                http.oauth2Login(oauth2 -> oauth2
                                .userInfoEndpoint(userInfo -> userInfo
                                                .userService(customOAuth2UserService))
                                .successHandler((request, response, authentication) -> {
                                        // 현재 어떤 소셜로 로그인했는지 ID 확인 (google, naver, kakao)
                                        String registrationId = ((OAuth2AuthenticationToken) authentication)
                                                        .getAuthorizedClientRegistrationId();
                                        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                                        Map<String, Object> attributes = oAuth2User.getAttributes();

                                        User user = null;

                                        if ("naver".equals(registrationId)) {
                                                Map<String, Object> responseMap = (Map<String, Object>) attributes
                                                                .get("response");
                                                if (responseMap == null) {
                                                        response.sendRedirect(allowedOrigin + "/login?error=naver_response_null");
                                                        return;
                                                }
                                                String mobileRaw = (String) responseMap.get("mobile");
                                                String mobile = (mobileRaw != null) ? mobileRaw.replace("-", "") : null;
                                                user = userService.socialLogin(
                                                                (String) responseMap.get("id"),
                                                                (String) responseMap.get("name"),
                                                                (String) responseMap.get("email"),
                                                                mobile,
                                                                (String) responseMap.get("id"),
                                                                Provider.NAVER);
                                        } else if ("kakao".equals(registrationId)) {
                                                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes
                                                                .get("kakao_account");
                                                if (kakaoAccount == null) {
                                                        response.sendRedirect(allowedOrigin + "/login?error=kakao_account_null");
                                                        return;
                                                }
                                                Map<String, Object> profile = (Map<String, Object>) kakaoAccount
                                                                .get("profile");
                                                String providerId = String.valueOf(attributes.get("id"));
                                                String nickname = (profile != null) ? (String) profile.get("nickname")
                                                                : "익명사용자";

                                                user = userService.socialLogin(providerId, nickname, null, null,
                                                                providerId, Provider.KAKAO);
                                        } else if ("google".equals(registrationId)) {
                                                String providerId = (String) attributes.get("sub");
                                                String name = (String) attributes.get("name");
                                                String email = (String) attributes.get("email");

                                                user = userService.socialLogin(providerId, name, email, null,
                                                                providerId, Provider.GOOGLE);
                                        }

                                        // DB에 저장되거나 조회된 유저 정보를 바탕으로 JWT 발급
                                        if (user != null) {
                                                String username = user.getUserId();
                                                String role = (user.getUserRole() == 0) ? "ROLE_ADMIN" : "ROLE_USER";

                                                String accessToken = jwtTokenUtil.generateAccessToken(username, role);
                                                String refreshToken = jwtTokenUtil.generateRefreshToken(username);

                                                // Redis에 refreshToken 저장 (TTL: 15일)
                                                redisTemplate.opsForValue().set(
                                                                "refreshToken:" + username,
                                                                refreshToken,
                                                                Duration.ofDays(15));

                                                // HttpOnly 쿠키로 토큰 전달
                                                ResponseCookie accessCookie = ResponseCookie
                                                                .from("accessToken", accessToken)
                                                                .httpOnly(true)
                                                                .path("/")
                                                                .maxAge(Duration.ofMinutes(15))
                                                                .sameSite("Lax")
                                                                .build();
                                                ResponseCookie refreshCookie = ResponseCookie
                                                                .from("refreshToken", refreshToken)
                                                                .httpOnly(true)
                                                                .path("/")
                                                                .maxAge(Duration.ofDays(15))
                                                                .sameSite("Lax")
                                                                .build();

                                                response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
                                                response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
                                                response.sendRedirect(allowedOrigin + "/login?loginSuccess=true");
                                        }
                                }));

                return http.build();
        }
}
// localStorage.getItem("accessToken")