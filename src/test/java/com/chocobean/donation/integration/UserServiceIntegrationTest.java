package com.chocobean.donation.integration;

import com.chocobean.donation.dto.SignUpForm;
import com.chocobean.donation.dto.UserResponse;
import com.chocobean.donation.entity.Provider;
import com.chocobean.donation.entity.User;
import com.chocobean.donation.repository.UserRepository;
import com.chocobean.donation.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService 통합 테스트")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 회원가입_및_조회_통합검증() {
        // Given: 유효한 회원가입 폼 데이터 준비
        SignUpForm form = new SignUpForm();
        form.setUserId("inttestuser");
        form.setUserName("통합테스트");
        form.setUserPassword("password123");
        form.setUserEmail("inttest@example.com");
        form.setUserPhone("010-9999-8888");

        // When: 회원가입 진행
        User savedUser = userService.signUp(form);

        // Then: DB 저장 상태 확인
        assertThat(savedUser.getUserNo()).isNotNull();
        assertThat(savedUser.getUserId()).isEqualTo("inttestuser");
        assertThat(passwordEncoder.matches("password123", savedUser.getUserPassword())).isTrue();
        assertThat(savedUser.getProvider()).isEqualTo(Provider.LOCAL);

        // When: 가입한 회원 아이디로 정보 조회
        UserResponse response = userService.getUserInfoByUserId("inttestuser");

        // Then: 조회된 데이터 확인
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo("inttestuser");
        assertThat(response.getUserName()).isEqualTo("통합테스트");
        assertThat(response.getUserEmail()).isEqualTo("inttest@example.com");
    }

    @Test
    void 비밀번호_변경_통합검증() {
        // Given: 회원가입 먼저 진행
        SignUpForm form = new SignUpForm();
        form.setUserId("pwchangeuser");
        form.setUserName("비번변경유저");
        form.setUserPassword("oldPassword123");
        form.setUserEmail("pw@example.com");
        form.setUserPhone("010-0000-1111");
        User savedUser = userService.signUp(form);
        
        Long userNo = savedUser.getUserNo();

        // When: 정상적인 비밀번호 변경 요청
        Map<String, String> inputData = new HashMap<>();
        inputData.put("currentPassword", "oldPassword123");
        inputData.put("newPassword", "newPassword456");
        inputData.put("confirmPassword", "newPassword456");
        
        String result = userService.updatePassword(inputData, userNo);

        // Then
        assertThat(result).isEqualTo("ok");
        
        // 실제 DB에 반영되었는지 확인
        User updatedUser = userRepository.findByUserNo(userNo).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword456", updatedUser.getUserPassword())).isTrue();
    }
}
