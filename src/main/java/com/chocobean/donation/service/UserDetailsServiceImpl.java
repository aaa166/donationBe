package com.chocobean.donation.service;

import com.chocobean.donation.repository.UserRepository;
import com.chocobean.donation.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    System.out.println("에러: DB에서 " + userId + " 사용자를 찾을 수 없습니다.");
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
                });

        // 🔹 DB 숫자 → Spring Security ROLE 문자열 변환
        String roleStr;
        switch (user.getUserRole()) {
            case 0: roleStr = "ROLE_ADMIN"; break;   // 관리자
            case 1: roleStr = "ROLE_USER"; break;    // 일반
            case 2: roleStr = "ROLE_COMPANY"; break; // 기업
            default: roleStr = "ROLE_USER"; break;
        }

        // 🔹 [중요 수정] 비밀번호가 null이거나 비어있는지 확인 (소셜 로그인 대응)
        // Spring Security User 생성자는 비밀번호에 null을 허용하지 않습니다.
        String password = user.getUserPassword();
        if (password == null || password.trim().isEmpty()) {
            // 소셜 로그인 유저는 비밀번호가 없으므로 임의의 값(더미)을 설정합니다.
            // {noop}은 비밀번호 인코딩을 하지 않겠다는 의미입니다.
            password = "{noop}SOCIAL_AUTH_USER_NO_PASSWORD";
        }

        // 🔹 [중요 수정] 아이디가 null인 경우도 방지
        String username = (user.getUserId() != null) ? user.getUserId() : userId;

        return new org.springframework.security.core.userdetails.User(
                username,
                password,
                List.of(new SimpleGrantedAuthority(roleStr))
        );
    }
}