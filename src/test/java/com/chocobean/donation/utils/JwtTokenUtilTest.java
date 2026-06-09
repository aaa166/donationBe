package com.chocobean.donation.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@DisplayName("JwtTokenUtil 단위 테스트")
class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private final String testSecret = "mySecretKeyForTestingDonationBeProjectSecretSecretSecret"; // 32바이트 이상

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtTokenUtil, "accessExpiration", 3600000L); // 1시간
        ReflectionTestUtils.setField(jwtTokenUtil, "refreshExpiration", 86400000L); // 24시간
    }

    @Nested
    @DisplayName("generateAccessToken(UserDetails) 메서드는")
    class GenerateAccessTokenWithUserDetailsTest {

        @Test
        void 액세스토큰생성_성공_UserDetails_정상권한() {
            // Given
            UserDetails userDetails = Mockito.mock(UserDetails.class);
            given(userDetails.getUsername()).willReturn("testuser");
            
            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            // Mockito wildcards 때문에 getAuthorities의 리턴 타입을 맞춤형태로 스텁
            Mockito.doReturn(authorities).when(userDetails).getAuthorities();

            // When
            String token = jwtTokenUtil.generateAccessToken(userDetails);

            // Then
            assertThat(token).isNotBlank();
            assertThat(jwtTokenUtil.getUsernameFromToken(token)).isEqualTo("testuser");
            assertThat(jwtTokenUtil.isTokenExpired(token)).isFalse();
            
            String role = jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("role", String.class));
            assertThat(role).isEqualTo("ROLE_USER");
        }

        @Test
        void 액세스토큰생성_성공_UserDetails_빈권한_기본역할() {
            // Given
            UserDetails userDetails = Mockito.mock(UserDetails.class);
            given(userDetails.getUsername()).willReturn("testuser");
            Mockito.doReturn(Collections.emptyList()).when(userDetails).getAuthorities();

            // When
            String token = jwtTokenUtil.generateAccessToken(userDetails);

            // Then
            assertThat(token).isNotBlank();
            String role = jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("role", String.class));
            assertThat(role).isEqualTo("ROLE_USER");
        }
    }

    @Nested
    @DisplayName("generateAccessToken(username, role) 메서드는")
    class GenerateAccessTokenWithUsernameAndRoleTest {

        @Test
        void 액세스토큰생성_성공_사용자명과_역할() {
            // When
            String token = jwtTokenUtil.generateAccessToken("admin", "ROLE_ADMIN");

            // Then
            assertThat(token).isNotBlank();
            assertThat(jwtTokenUtil.getUsernameFromToken(token)).isEqualTo("admin");
            String role = jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("role", String.class));
            assertThat(role).isEqualTo("ROLE_ADMIN");
        }

        @Test
        void 액세스토큰생성_성공_역할null시_기본역할() {
            // When
            String token = jwtTokenUtil.generateAccessToken("user", null);

            // Then
            assertThat(token).isNotBlank();
            String role = jwtTokenUtil.getClaimFromToken(token, claims -> claims.get("role", String.class));
            assertThat(role).isEqualTo("ROLE_USER");
        }
    }

    @Nested
    @DisplayName("generateRefreshToken 메서드는")
    class GenerateRefreshTokenTest {

        @Test
        void 리프레시토큰생성_성공() {
            // When
            String token = jwtTokenUtil.generateRefreshToken("testuser");

            // Then
            assertThat(token).isNotBlank();
            assertThat(jwtTokenUtil.getUsernameFromToken(token)).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("isTokenExpired 메서드는")
    class IsTokenExpiredTest {

        @Test
        void 토큰만료여부_성공_만료된경우_true() {
            // Given
            JwtTokenUtil expiredTokenUtil = new JwtTokenUtil();
            ReflectionTestUtils.setField(expiredTokenUtil, "secret", testSecret);
            ReflectionTestUtils.setField(expiredTokenUtil, "accessExpiration", -1000L); // 만료시간 1초 전으로 설정
            ReflectionTestUtils.setField(expiredTokenUtil, "refreshExpiration", -1000L);

            String expiredToken = expiredTokenUtil.generateAccessToken("testuser", "ROLE_USER");

            // When
            boolean isExpired = jwtTokenUtil.isTokenExpired(expiredToken);

            // Then
            assertThat(isExpired).isTrue();
        }

        @Test
        void 토큰만료여부_성공_만료되지않은경우_false() {
            // Given
            String token = jwtTokenUtil.generateAccessToken("testuser", "ROLE_USER");

            // When
            boolean isExpired = jwtTokenUtil.isTokenExpired(token);

            // Then
            assertThat(isExpired).isFalse();
        }
    }

    @Nested
    @DisplayName("validateToken 메서드는")
    class ValidateTokenTest {

        @Test
        void 토큰검증_성공_정상토큰_true() {
            // Given
            String token = jwtTokenUtil.generateAccessToken("testuser", "ROLE_USER");
            UserDetails userDetails = Mockito.mock(UserDetails.class);
            given(userDetails.getUsername()).willReturn("testuser");

            // When
            Boolean isValid = jwtTokenUtil.validateToken(token, userDetails);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        void 토큰검증_실패_이름불일치_false() {
            // Given
            String token = jwtTokenUtil.generateAccessToken("testuser", "ROLE_USER");
            UserDetails userDetails = Mockito.mock(UserDetails.class);
            given(userDetails.getUsername()).willReturn("anotheruser");

            // When
            Boolean isValid = jwtTokenUtil.validateToken(token, userDetails);

            // Then
            assertThat(isValid).isFalse();
        }
    }
}
