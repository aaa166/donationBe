package com.chocobean.donation.service;

import com.chocobean.donation.dto.*;
import com.chocobean.donation.entity.Donation;
import com.chocobean.donation.entity.Payment;
import com.chocobean.donation.entity.Provider;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.PaymentRepository;
import com.chocobean.donation.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("signUp 메서드는")
    class SignUpTest {

        private SignUpForm createValidSignUpForm() {
            SignUpForm form = new SignUpForm();
            form.setUserId("testuser");
            form.setUserName("홍길동");
            form.setUserPassword("password123");
            form.setUserEmail("test@example.com");
            form.setUserPhone("010-1234-5678");
            return form;
        }

        @Test
        void 회원가입_성공_유효한_정보() {
            // Given
            SignUpForm form = createValidSignUpForm();
            given(userRepository.existsByUserId(form.getUserId())).willReturn(false);
            given(passwordEncoder.encode(form.getUserPassword())).willReturn("encryptedPassword123");

            User savedUser = new User();
            savedUser.setUserNo(1L);
            savedUser.setUserId(form.getUserId());
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // When
            User result = userService.signUp(form);

            // Then
            verify(userRepository, times(1)).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();

            assertThat(capturedUser.getUserId()).isEqualTo("testuser");
            assertThat(capturedUser.getUserName()).isEqualTo("홍길동");
            assertThat(capturedUser.getUserPassword()).isEqualTo("encryptedPassword123");
            assertThat(capturedUser.getUserEmail()).isEqualTo("test@example.com");
            assertThat(capturedUser.getUserPhone()).isEqualTo("010-1234-5678");
            assertThat(capturedUser.getUserRole()).isEqualTo(1);
            assertThat(capturedUser.getUserState()).isEqualTo("A");
            assertThat(capturedUser.getTotalAmount()).isEqualTo(0L);
            assertThat(capturedUser.getProvider()).isEqualTo(Provider.LOCAL);

            assertThat(result.getUserNo()).isEqualTo(1L);
        }

        @Test
        void 회원가입_실패_잘못된_아이디형식() {
            // Given
            SignUpForm form = createValidSignUpForm();
            // 아이디 조건: 첫글자 소문자 영어, 두번째부터 영어/숫자 3글자 이상 (정규식: ^[a-z][a-z0-9]{2,}$)
            form.setUserId("1ab"); // 영어 시작 아님

            // When & Then
            assertThatThrownBy(() -> userService.signUp(form))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("아이디 형식이 올바르지 않습니다.");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void 회원가입_실패_중복된_아이디() {
            // Given
            SignUpForm form = createValidSignUpForm();
            given(userRepository.existsByUserId(form.getUserId())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.signUp(form))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미 사용 중인 아이디입니다.");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("getUserInfoByUserId 메서드는")
    class GetUserInfoByUserIdTest {

        @Test
        void 유저정보조회_성공_존재하는_아이디() {
            // Given
            String userId = "testuser";
            User user = new User();
            user.setUserNo(1L);
            user.setUserId(userId);
            user.setUserName("홍길동");
            user.setUserEmail("test@example.com");

            given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));

            // When
            UserResponse response = userService.getUserInfoByUserId(userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(userId);
            assertThat(response.getUserName()).isEqualTo("홍길동");
            assertThat(response.getUserEmail()).isEqualTo("test@example.com");
        }

        @Test
        void 유저정보조회_실패_존재하지않는_아이디() {
            // Given
            String userId = "nonexistent";
            given(userRepository.findByUserId(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getUserInfoByUserId(userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 사용자를 찾을 수 없습니다. ID: nonexistent");
        }
    }

    @Nested
    @DisplayName("getDonationListByUserId 메서드는")
    class GetDonationListByUserIdTest {

        @Test
        void 기부내역조회_성공() {
            // Given
            String userId = "testuser";
            User user = new User();
            user.setUserNo(1L);
            user.setUserId(userId);

            Donation donation = new Donation();
            donation.setDonationTitle("어린이 돕기 기부");

            Payment payment1 = new Payment();
            payment1.setPayAmount(10000L);
            payment1.setPayDate(LocalDate.of(2026, 6, 1));
            payment1.setDonation(donation);

            Payment payment2 = new Payment();
            payment2.setPayAmount(20000L);
            payment2.setPayDate(LocalDate.of(2026, 6, 5));
            payment2.setDonation(donation);

            given(userRepository.findByUserId(userId)).willReturn(Optional.of(user));
            given(paymentRepository.findPaymentsWithDonationByUserNo(user.getUserNo()))
                    .willReturn(List.of(payment1, payment2));

            // When
            List<MyDonation> result = userService.getDonationListByUserId(userId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getDonationTitle()).isEqualTo("어린이 돕기 기부");
            assertThat(result.get(0).getPayAmount()).isEqualTo(10000L);
            assertThat(result.get(0).getPayDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(result.get(1).getPayAmount()).isEqualTo(20000L);
        }

        @Test
        void 기부내역조회_실패_존재하지않는_유저() {
            // Given
            String userId = "nonexistent";
            given(userRepository.findByUserId(userId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.getDonationListByUserId(userId))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("updatePassword 메서드는")
    class UpdatePasswordTest {

        @Test
        void 비밀번호변경_성공_정상조건() {
            // Given
            Long userNo = 1L;
            User user = new User();
            user.setUserNo(userNo);
            user.setUserPassword("encodedCurrentPassword");

            Map<String, String> inputData = new HashMap<>();
            inputData.put("currentPassword", "currentPassword123");
            inputData.put("newPassword", "newPassword123");
            inputData.put("confirmPassword", "newPassword123");

            given(userRepository.findByUserNo(userNo)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("currentPassword123", "encodedCurrentPassword")).willReturn(true);
            given(passwordEncoder.encode("newPassword123")).willReturn("encodedNewPassword");

            // When
            String result = userService.updatePassword(inputData, userNo);

            // Then
            assertThat(result).isEqualTo("ok");
            assertThat(user.getUserPassword()).isEqualTo("encodedNewPassword");
        }

        @Test
        void 비밀번호변경_실패_현재비밀번호_불일치() {
            // Given
            Long userNo = 1L;
            User user = new User();
            user.setUserNo(userNo);
            user.setUserPassword("encodedCurrentPassword");

            Map<String, String> inputData = new HashMap<>();
            inputData.put("currentPassword", "wrongPassword");
            inputData.put("newPassword", "newPassword123");
            inputData.put("confirmPassword", "newPassword123");

            given(userRepository.findByUserNo(userNo)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongPassword", "encodedCurrentPassword")).willReturn(false);

            // When
            String result = userService.updatePassword(inputData, userNo);

            // Then
            assertThat(result).isEqualTo("unauthorized");
            // 비밀번호가 변하지 않았는지 검증
            assertThat(user.getUserPassword()).isEqualTo("encodedCurrentPassword");
        }

        @Test
        void 비밀번호변경_실패_새비밀번호_확인_불일치() {
            // Given
            Long userNo = 1L;
            User user = new User();
            user.setUserNo(userNo);
            user.setUserPassword("encodedCurrentPassword");

            Map<String, String> inputData = new HashMap<>();
            inputData.put("currentPassword", "currentPassword123");
            inputData.put("newPassword", "newPassword123");
            inputData.put("confirmPassword", "differentPassword123");

            given(userRepository.findByUserNo(userNo)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("currentPassword123", "encodedCurrentPassword")).willReturn(true);

            // When
            String result = userService.updatePassword(inputData, userNo);

            // Then
            assertThat(result).isEqualTo("mismatch");
            assertThat(user.getUserPassword()).isEqualTo("encodedCurrentPassword");
        }

        @Test
        void 비밀번호변경_실패_존재하지않는_유저() {
            // Given
            Long userNo = 999L;
            Map<String, String> inputData = new HashMap<>();
            given(userRepository.findByUserNo(userNo)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.updatePassword(inputData, userNo))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("존재하지 않는 유저입니다.");
        }
    }

    @Nested
    @DisplayName("socialLogin 메서드는")
    class SocialLoginTest {

        @Test
        void 소셜로그인_성공_기존유저() {
            // Given
            String providerId = "socialId123";
            Provider provider = Provider.KAKAO;
            User existingUser = new User();
            existingUser.setUserNo(10L);
            existingUser.setUserId("K_testuser");
            existingUser.setProvider(provider);
            existingUser.setProviderId(providerId);

            given(userRepository.findByProviderAndProviderId(provider, providerId))
                    .willReturn(Optional.of(existingUser));

            // When
            User result = userService.socialLogin("testuser", "카카오유저", "kakao@test.com", "010-0000-0000", providerId, provider);

            // Then
            assertThat(result).isEqualTo(existingUser);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void 소셜로그인_성공_신규유저_가입() {
            // Given
            String providerId = "naverId123";
            Provider provider = Provider.NAVER;

            given(userRepository.findByProviderAndProviderId(provider, providerId))
                    .willReturn(Optional.empty());

            User savedUser = new User();
            savedUser.setUserNo(20L);
            savedUser.setUserId("N_testuser");
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            // When
            User result = userService.socialLogin("testuser", "네이버유저", "naver@test.com", "010-1111-1111", providerId, provider);

            // Then
            verify(userRepository, times(1)).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();

            assertThat(capturedUser.getUserId()).isEqualTo("N_testuser");
            assertThat(capturedUser.getUserName()).isEqualTo("네이버유저");
            assertThat(capturedUser.getUserEmail()).isEqualTo("naver@test.com");
            assertThat(capturedUser.getUserPhone()).isEqualTo("010-1111-1111");
            assertThat(capturedUser.getProvider()).isEqualTo(provider);
            assertThat(capturedUser.getProviderId()).isEqualTo(providerId);
            assertThat(capturedUser.getUserRole()).isEqualTo(1);
            assertThat(capturedUser.getUserState()).isEqualTo("A");
            assertThat(capturedUser.getTotalAmount()).isEqualTo(0L);

            assertThat(result).isEqualTo(savedUser);
        }
    }
}
